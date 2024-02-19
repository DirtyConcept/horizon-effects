package net.horizonmines.effects.effects.death;

import com.destroystokyo.paper.ParticleBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.horizonmines.effects.base.CustomTask;
import net.horizonmines.effects.effects.EffectPlayer;
import net.horizonmines.effects.entities.OptimizedStand;
import net.horizonmines.effects.entities.EntityManager;
import net.horizonmines.effects.utils.PacketUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minecraft.core.Rotations;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Singleton
public class GhostsEffect implements EffectPlayer {
    private final Plugin plugin;
    private final EntityManager entityManager;

    @Inject
    public GhostsEffect(Plugin plugin,
                        EntityManager entityManager) {
        this.plugin = plugin;
        this.entityManager = entityManager;
    }

    @Override
    public void play(@NotNull Player player) {
        final GhostTask task = new GhostTask(plugin, player, entityManager);
        task.spawnGhosts();
        task.start();

        final Location loc = player.getLocation();
        player.getWorld().playSound(Sound.sound(Key.key("item.trident.thunder"), Sound.Source.MASTER, 1.2f, 1.75f), loc.getX(), loc.getY(), loc.getZ());
    }

    private static final class GhostTask extends CustomTask {
        private final Plugin plugin;
        private final Location center;
        private final CraftPlayer player;
        private int ticksElapsed = 1; // Set to 1 to prevent the task from doing problem with 0 / x = infinity
        private final List<OptimizedStand> ghosts;
        private final EntityManager entityManager;


        public GhostTask(Plugin plugin, Player player, EntityManager entityManager) {
            super(plugin, 1L, 1L, true, true);
            this.center = player.getLocation();
            this.entityManager = entityManager;
            this.ghosts = new ArrayList<>(4); // Preallocate the list size
            this.plugin = plugin;
            this.player = (CraftPlayer) player;
        }

        @Override
        public void run() {
            if (ticksElapsed >= 41) {
                entityManager.removeEntities(ghosts);

                final ParticleBuilder particle = new ParticleBuilder(Particle.CLOUD)
                        .source(player)
                        .extra(0.1)
                        .count(10);

                final Sound sound = Sound.sound(Key.key("entity.allay.death"), Sound.Source.MASTER, 0.6f, 2f);
                for (final OptimizedStand armorStand : ghosts) {
                    final Location loc = new Location(center.getWorld(), armorStand.getX(), armorStand.getY() + 1.5, armorStand.getZ());
                    particle.location(loc).spawn();
                    player.getWorld().playSound(sound, loc.getX(), loc.getY(), loc.getZ());
                    armorStand.remove(Entity.RemovalReason.DISCARDED);
                }

                cancel();
                return;
            }

            List<Packet<?>> updatePackets = new ArrayList<>(ghosts.size() * 2);
            ParticleBuilder particle = new ParticleBuilder(Particle.SUSPENDED_DEPTH)
                    .source(player)
                    .extra(0)
                    .count(1);

            Set<ServerPlayer> viewers = new HashSet<>();
            for (OptimizedStand armorStand : ghosts) {
                particle.location(center.getWorld(), armorStand.getX(), armorStand.getY() + 1.3475, armorStand.getZ()).spawn();

                armorStand.setYRot(armorStand.getYRot() + 10f);
                armorStand.setXRot(armorStand.getXRot() - 5f);

                double yawRadians = Math.toRadians(armorStand.getYRot());
                double pitchRadians = Math.toRadians(armorStand.getXRot());
                armorStand.setHeadPose(new Rotations((float) pitchRadians, (float) yawRadians, 0f));

                double cosPitch = Math.cos(pitchRadians);
                double velocityX = center.getX() + (0.3d * Math.sin(yawRadians) * cosPitch);
                double velocityY = center.getY() + ((0.3d / (ticksElapsed / 10d)) * Math.sin(pitchRadians));
                double velocityZ = center.getZ() + (0.3d * Math.cos(yawRadians) * cosPitch);

                short dx = (short) ((velocityX * 32.0 - center.getX() * 32.0) * 128.0);
                short dy = (short) ((velocityY * 32.0 - center.getY() * 32.0) * 128.0);
                short dz = (short) ((velocityZ * 32.0 - center.getZ() * 32.0) * 128.0);
                byte yaw = (byte) (armorStand.getYRot() * 256.0f / 360.0f);
                byte pitch = (byte) (armorStand.getXRot() * 256.0f / 180.0f);

                plugin.getServer().getScheduler().callSyncMethod(plugin, () -> {
                    armorStand.move(MoverType.SELF, new Vec3(velocityX - center.getX(), velocityY - center.getY(), velocityZ - center.getZ()));
                    return null;
                });

                final ClientboundSetEntityDataPacket dataPacket = new ClientboundSetEntityDataPacket(armorStand.getId(), armorStand.getEntityData(), true);
                final ClientboundMoveEntityPacket.PosRot entityMovement = new ClientboundMoveEntityPacket.PosRot(armorStand.getId(), dx, dy, dz, yaw, pitch, false);

                updatePackets.add(dataPacket);
                updatePackets.add(entityMovement);
                viewers.clear();

                // update ghost only for players who can view them
                viewers.addAll(entityManager.getViewers(armorStand));
                PacketUtils.sendPackets(viewers, dataPacket, entityMovement);
            }

            ticksElapsed++;
        }

        private void spawnGhosts() {
            final Level level = ((CraftWorld) center.getWorld()).getHandle();
            // Create and update the game profile of the skull.
            final ItemStack playerSkull = new ItemStack(Items.PLAYER_HEAD);
            playerSkull.addTagElement(PlayerHeadItem.TAG_SKULL_OWNER, NbtUtils.writeGameProfile(new CompoundTag(), player.getProfile()));

            float yawOffset = (float) (Math.random() * 360f);
            double xOffset, zOffset;

            for (int i = 0; i < 4; i++) {
                xOffset = (Math.random() - 0.5) * 0.5;
                zOffset = (Math.random() - 0.5) * 0.5;
                final OptimizedStand armorStand = new OptimizedStand(level, center.getX() + xOffset, center.getY(), center.getZ() + zOffset);
                armorStand.setRot(yawOffset, 40);
                armorStand.setInvisible(true);
                armorStand.setNoGravity(true);
                armorStand.setItemSlot(EquipmentSlot.HEAD, playerSkull);
                armorStand.noPhysics = true;

                ghosts.add(armorStand);
                entityManager.addEntity(armorStand);

                yawOffset += 90;
                if (yawOffset >= 180) {
                    yawOffset -= 360;
                }
            }

            final ParticleBuilder particle = new ParticleBuilder(Particle.SOUL)
                    .source(player)
                    .extra(0.5)
                    .count(20)
                    .location(center.add(0, 1.5, 0));
            particle.spawn();
        }
    }
}






