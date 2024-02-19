package net.horizonmines.effects.effects.kill;

import com.destroystokyo.paper.ParticleBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.horizonmines.effects.base.CustomTask;
import net.horizonmines.effects.effects.EffectPlayer;
import net.horizonmines.effects.entities.EntityManager;
import net.horizonmines.effects.utils.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Singleton
public class ExplosionEffect implements EffectPlayer {
    private final Plugin plugin;
    private final EntityManager entityManager;

    @Inject
    public ExplosionEffect(Plugin plugin, EntityManager entityManager) {
        this.plugin = plugin;
        this.entityManager = entityManager;
    }

    @Override
    public void play(@NotNull Player player) {
        Location loc = player.getLocation();
        ParticleBuilder particle = new ParticleBuilder(Particle.EXPLOSION_LARGE)
                .allPlayers()
                .count(1)
                .source(player)
                .location(loc.add(0, 1, 0));
        particle.spawn();
        new KaboomTask(plugin, player, entityManager).start();
    }


    private static class KaboomTask extends CustomTask {
        private final Random random;
        private final Location location;
        private int ticksElapsed;
        private final ParticleBuilder particle;
        private final EntityManager entityManager;
        private final Set<FallingBlockEntity> fallingBlocks;

        public KaboomTask(Plugin plugin, Player player, EntityManager entityManager) {
            super(plugin, 0, 2, true, true);
            this.location = player.getLocation();
            this.entityManager = entityManager;
            this.ticksElapsed = 0;
            this.random = new Random();
            this.fallingBlocks = new HashSet<>();
            Level level = ((CraftWorld)player.getLocation().getWorld()).getHandle();

            for (int i = 0; i < 1; i++) {
                Location blockPos = player.getLocation().add(0.5, 0, 0.5);
                FallingBlockEntity entity = new FallingBlockEntity(level, blockPos.getBlockX(), blockPos.getBlockY(), blockPos.getBlockZ(),
                        ((CraftBlock)blockPos.getBlock()).getNMS());
                double deltaX = (blockPos.getX() - location.getX()) * 0.1;
                double deltaY = 0.3;
                double deltaZ = (blockPos.getZ() - location.getZ()) * 0.1;
                entity.setDeltaMovement(deltaX, deltaY, deltaZ);
            }


            for (FallingBlockEntity entity : fallingBlocks) {
                ClientboundTeleportEntityPacket tpPacket = new ClientboundTeleportEntityPacket(entity);
                ClientboundSetEntityDataPacket dataPacket = new ClientboundSetEntityDataPacket(entity.getId(), entity.getEntityData(), true);
                ClientboundSetEntityMotionPacket motionPacket = new ClientboundSetEntityMotionPacket(entity);
                entityManager.addEntity(entity);
                PacketUtils.sendPackets(entityManager.getViewers(entity), tpPacket, dataPacket, motionPacket);
            }

            this.particle = new ParticleBuilder(Particle.EXPLOSION_LARGE)
                    .count(1)
                    .source(player);
        }

        @Override
        public void run() {
            if (ticksElapsed >= 60) {
                entityManager.removeEntities(fallingBlocks);
                this.cancel();
                return;
            }

            spawnExplosionParticle(location.getX(), location.getY(), location.getZ());
            ticksElapsed++;
        }

        private void spawnExplosionParticle(double x, double y, double z) {
            for (int i = 0; i < 2; i++) {
                particle.location(location.getWorld(), x + (1d + random.nextDouble()), y + (2d + (random.nextDouble() * 2d)), z + (1d + random.nextDouble()))
                        .spawn();
            }
        }
    }
}
