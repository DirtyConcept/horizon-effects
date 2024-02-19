package net.horizonmines.effects.effects.death;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
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
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class BeheadedEffect implements EffectPlayer {
    private final Plugin plugin;
    private final EntityManager entityManager;

    @Inject
    public BeheadedEffect(Plugin plugin,
                          EntityManager entityManager) {
        this.plugin = plugin;
        this.entityManager = entityManager;
    }

    @Override
    public void play(@NotNull Player player) {
        BeheadedTask task = new BeheadedTask(plugin, entityManager, player);
        task.start();
    }

    private static final class BeheadedTask extends CustomTask {

        private static final double[] X_OFFSET = {-0.375, -0.625, -0.375, 0.0, 0.0, 0.0, 0.375, 0.625, 0.375};
        private static final double[] Z_OFFSET = {-0.375, 0.0, 0.375, -0.625, 0.0, 0.625, -0.375, 0.0, 0.375};
        private final Plugin plugin;
        private final EntityManager entityManager;

        private final Location center;
        private final CraftPlayer player;
        private final List<OptimizedStand> armorStands;
        private int ticksElapsed = 0;

        private boolean bloodSpawned;
        public BeheadedTask(final @NotNull Plugin plugin,
                            final @NotNull EntityManager entityManager,
                            final @NotNull Player player) {
            super(plugin, 0L, 2L, true, true);
            this.plugin = plugin;
            this.entityManager = entityManager;
            this.center = player.getLocation();
            this.armorStands = new ArrayList<>();
            this.player = (CraftPlayer) player;
            spawnHead();
            center.getWorld().playSound(
                    Sound.sound(Key.key("entity.player.attack.sweep"), Sound.Source.MASTER, 10f, 0.75f), center.getX(), center.getY(), center.getZ()
            );
        }

        @Override
        public void run() {
            // End the beheaded task thingy.
            if (ticksElapsed >= 40) {
                entityManager.removeEntities(armorStands);

                for (final OptimizedStand armorStand : armorStands) {
                    armorStand.remove(Entity.RemovalReason.DISCARDED);
                }

                cancel();
                return;
            }

            List<Packet<?>> packets = new ArrayList<>();
            OptimizedStand armorStand = armorStands.get(0);
            Location newLoc = new Location(center.getWorld(), armorStand.getX(), armorStand.getBlockY() - 1, armorStand.getZ());
            if (isNotColliding(newLoc.add(0, 1, 0), 0.5d) && !bloodSpawned) {
                Rotations rotations = armorStand.getHeadPose();
                armorStand.setHeadPose(new Rotations(0, 0, rotations.getZ() + 10f));
                packets.add(new ClientboundSetEntityDataPacket(armorStand.getId(), armorStand.getEntityData(), true));
                newLoc.subtract(0, 1, 0);

                short dx = (short) ((newLoc.getX() * 32.0 - armorStand.getX() * 32.0) * 128.0);
                short dy = (short) ((newLoc.getY() * 32.0 - armorStand.getBlockY() * 32.0) * 128.0);
                short dz = (short) ((newLoc.getZ() * 32.0 - armorStand.getZ() * 32.0) * 128.0);

                // Client-side movement
                packets.add(new ClientboundMoveEntityPacket.Pos(armorStand.getId(), dx, dy, dz, true));
                // Server-side movement
                plugin.getServer().getScheduler().callSyncMethod(plugin, () -> {
                    armorStand.move(MoverType.SELF, new Vec3(
                            newLoc.getX() - armorStand.getX(),
                            newLoc.getY() - armorStand.getBlockY(),
                            newLoc.getZ() - armorStand.getZ()
                    ));
                    return null;
                });

            } else if (!bloodSpawned) {
                Rotations rotations = armorStand.getHeadPose();
                armorStand.setHeadPose(new Rotations(0, 0, rotations.getZ() + 10f));
                packets.add(new ClientboundSetEntityDataPacket(armorStand.getId(), armorStand.getEntityData(), true));

                // todo: calculate not only the y offset of the head not only for rotation, but also do it by block hitbox size like slabs n stuff
                newLoc.add(0, calculateYOffset(rotations.getZ()), 0);

                short dx = (short) ((newLoc.getX() * 32.0 - armorStand.getX() * 32.0) * 128.0);
                short dy = (short) ((newLoc.getY() * 32.0 - armorStand.getBlockY() * 32.0) * 128.0);
                short dz = (short) ((newLoc.getZ() * 32.0 - armorStand.getZ() * 32.0) * 128.0);

                // Client-side movement
                packets.add(new ClientboundMoveEntityPacket.Pos(armorStand.getId(), dx, dy, dz, true));
                // Server-side movement
                plugin.getServer().getScheduler().callSyncMethod(plugin, () -> {
                    armorStand.move(MoverType.SELF, new Vec3(
                            newLoc.getX() - armorStand.getX(),
                            newLoc.getY() - armorStand.getBlockY(),
                            newLoc.getZ() - armorStand.getZ()
                    ));
                    return null;
                });

                spawnBlood();
                bloodSpawned = true;

                center.getWorld().playSound(
                        Sound.sound(Key.key("entity.player.death"), Sound.Source.MASTER, 10f, 0.1f), center.getX(), center.getY(), center.getZ()
                );
            }

            if (!packets.isEmpty()) {
                PacketUtils.sendPackets(entityManager.getViewers(armorStand), packets);
            }
            ticksElapsed++;
        }

        private double calculateYOffset(final float zRot) {
            float headZRotation = zRot % 360; // make sure it goes from 0 to 360 (changes to 0)
            double yOffset;
            if (headZRotation <= 180 && headZRotation >= 0) {
                yOffset = -0.44 + (headZRotation * (11d / 3000d));
            } else if (headZRotation >= 180 && headZRotation < 360) {
                yOffset = -0.44 + ((360 - headZRotation) * (11d / 3000d));
            } else {
                yOffset = -0.44;
            }
            return yOffset;
        }

        private boolean isNotColliding(final @NotNull Location headLoc,
                                       final double dimension) {
            double checkRange = dimension / 2;

            for (double x = headLoc.getX() - checkRange; x <= headLoc.getX() + checkRange; x += dimension) {
                for (double z = headLoc.getZ() - checkRange; z <= headLoc.getZ() + checkRange; z += dimension) {
                    Location blockLoc = new Location(headLoc.getWorld(), x, headLoc.getY(), z);
                    if (!blockLoc.getBlock().isPassable()) {
                        return false;  // If any block is not passable, it's on the ground
                    }
                }
            }

            return true;  // If all blocks are passable, it's not on the ground
        }

        private void spawnHead() {
            final ItemStack playerSkull = new ItemStack(Items.PLAYER_HEAD);
            final GameProfile profile = this.player.getProfile();
            playerSkull.addTagElement(PlayerHeadItem.TAG_SKULL_OWNER, NbtUtils.writeGameProfile(new CompoundTag(), profile));

            final Level level = ((CraftWorld) center.getWorld()).getHandle();
            Location currLoc = new Location(center.getWorld(), center.getX(), center.getY(), center.getZ());

            final OptimizedStand playerHead = new OptimizedStand(level, currLoc.getX(), currLoc.getBlockY(), currLoc.getZ());
            playerHead.setItemSlot(EquipmentSlot.HEAD, playerSkull);
            playerHead.setInvisible(true);
            playerHead.setNoGravity(true);
            playerHead.setRot(center.getYaw(), center.getPitch());
            playerHead.setHeadPose(new Rotations(playerHead.getXRot(), playerHead.getYRot(), 0));
            playerHead.noPhysics = false;

            armorStands.add(playerHead);
            entityManager.addEntity(playerHead);
        }

        private void spawnBlood() {
            final Level level = ((CraftWorld) center.getWorld()).getHandle();
            for (int i = 0; i < X_OFFSET.length; i++) {

                // todo: fix blood spawning higher than needed.
                Location currLoc = new Location(center.getWorld(), center.getX() + X_OFFSET[i], center.getBlockY() - 1.95 + 1, center.getZ() + Z_OFFSET[i]);
                while (isNotColliding(currLoc, 0.02d)) {
                    currLoc.subtract(0, 1, 0);
                }

                currLoc.subtract(0, 1, 0);
                final OptimizedStand blood = new OptimizedStand(level, currLoc.getX(), currLoc.getY(), currLoc.getZ());
                blood.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.RED_CONCRETE));
                blood.setInvisible(true);
                blood.setNoGravity(true);
                blood.setRot(0, 0);
                blood.setHeadPose(new Rotations(0, 0, 0));
                blood.noPhysics = false;

                armorStands.add(blood);
                entityManager.addEntity(blood);
            }
        }
    }

    @Deprecated
    private static final class SimpleBeheadedTask extends CustomTask {
        private final Location center;
        private final CraftPlayer player;
        private final List<OptimizedStand> armorStands;
        private static final double[] X_OFFSET = {-0.375, -0.625, -0.375, 0.0, 0.0, 0.0, 0.375, 0.625, 0.375};
        private static final double[] Z_OFFSET = {-0.375, 0.0, 0.375, -0.625, 0.0, 0.625, -0.375, 0.0, 0.375};
        private boolean bloodSpawned;

        public SimpleBeheadedTask(final @NotNull Plugin plugin,
                                  final @NotNull Player player) {
            super(plugin, 40L, 0, false, true);
            this.center = player.getLocation();
            this.armorStands = new ArrayList<>();
            this.player = (CraftPlayer) player;

            spawnHead();
            spawnBlood();
        }

        @Override
        public void run() {
            final int[] armorStandIds = new int[armorStands.size()];
            for (int i = 0; i < armorStands.size(); i++) {
                armorStandIds[i] = armorStands.get(i).getId();
            }

            final ClientboundRemoveEntitiesPacket removeEntity = new ClientboundRemoveEntitiesPacket(armorStandIds);
            PacketUtils.sendAllPacket(center.getWorld(), removeEntity);

            for (final OptimizedStand armorStand : armorStands) {
                armorStand.remove(Entity.RemovalReason.DISCARDED);
            }

            armorStands.clear();
            cancel();
        }

        private void spawnHead() {
            final ItemStack playerSkull = new ItemStack(Items.PLAYER_HEAD);
            final GameProfile profile = this.player.getProfile();
            playerSkull.addTagElement(PlayerHeadItem.TAG_SKULL_OWNER, NbtUtils.writeGameProfile(new CompoundTag(), profile));

            final Level level = ((CraftWorld) center.getWorld()).getHandle();
            Location currLoc = new Location(center.getWorld(), center.getX(), center.getBlockY() - 1.3 + 1, center.getZ());
            while (currLoc.getBlock().isPassable()) {
                currLoc.subtract(0, 1, 0);
            }

            currLoc.subtract(0, 1, 0);
            final OptimizedStand playerHead = new OptimizedStand(level, currLoc.getX(), currLoc.getY(), currLoc.getZ());
            playerHead.setItemSlot(EquipmentSlot.HEAD, playerSkull);
            playerHead.setInvisible(true);
            playerHead.setNoGravity(true);
            playerHead.setRot(0, 0);
            playerHead.setHeadPose(new Rotations(playerHead.getXRot(), playerHead.getYRot(), 90));
            playerHead.noPhysics = true;

            armorStands.add(playerHead);
            final ClientboundAddEntityPacket addEntity = new ClientboundAddEntityPacket(playerHead, playerHead.getId());
            final ClientboundSetEntityDataPacket dataPacket = new ClientboundSetEntityDataPacket(playerHead.getId(), playerHead.getEntityData(), true);
            final ClientboundSetEquipmentPacket equipmentPacket = new ClientboundSetEquipmentPacket(
                    playerHead.getId(), List.of(new Pair<>(EquipmentSlot.HEAD, playerHead.getItemBySlot(EquipmentSlot.HEAD)))
            );
            PacketUtils.sendAllPackets(center.getWorld(), addEntity, dataPacket, equipmentPacket);
        }

        private void spawnBlood() {
            final Level level = ((CraftWorld) center.getWorld()).getHandle();
            for (int i = 0; i < X_OFFSET.length; i++) {
                final OptimizedStand blood = buildArmorStand(i, level);
                blood.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.RED_CONCRETE));
                blood.setInvisible(true);
                blood.setNoGravity(true);
                blood.setRot(0, 0);
                blood.setHeadPose(new Rotations(blood.getXRot(), blood.getYRot(), 0));
                blood.noPhysics = true;

                armorStands.add(blood);
                final ClientboundAddEntityPacket addEntity = new ClientboundAddEntityPacket(blood, blood.getId());
                final ClientboundSetEntityDataPacket dataPacket = new ClientboundSetEntityDataPacket(blood.getId(), blood.getEntityData(), true);
                final ClientboundSetEquipmentPacket equipmentPacket = new ClientboundSetEquipmentPacket(
                        blood.getId(), List.of(new Pair<>(EquipmentSlot.HEAD, blood.getItemBySlot(EquipmentSlot.HEAD)))
                );

                PacketUtils.sendAllPackets(center.getWorld(), addEntity, dataPacket, equipmentPacket);
            }
        }

        @NotNull
        private OptimizedStand buildArmorStand(int i, Level level) {
            double xOffset = X_OFFSET[i];
            double zOffset = Z_OFFSET[i];
            Location currLoc = new Location(center.getWorld(), center.getX() + xOffset, center.getBlockY() - 1.95 + 1, center.getZ() + zOffset);
            while (currLoc.getBlock().isPassable()) {
                currLoc.subtract(0, 1, 0);
            }

            currLoc.subtract(0, 1, 0);
            return new OptimizedStand(level, currLoc.getX(), currLoc.getY(), currLoc.getZ());
        }
    }
}
