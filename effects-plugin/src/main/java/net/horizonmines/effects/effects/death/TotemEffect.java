package net.horizonmines.effects.effects.death;

import com.destroystokyo.paper.ParticleBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.horizonmines.effects.base.CustomTask;
import net.horizonmines.effects.effects.EffectPlayer;
import net.horizonmines.effects.entities.EntityManager;
import net.horizonmines.effects.utils.PacketUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

@Singleton
public class TotemEffect implements EffectPlayer {
    private final Plugin plugin;
    private final long duration; // Duration in ticks
    private final int particleCount;
    private final EntityManager entityManager;

    @Inject
    public TotemEffect(Plugin plugin, EntityManager entityManager) {
        this.plugin = plugin;
        this.entityManager = entityManager;
        this.duration = 10;
        this.particleCount = 10;
    }

    @Override
    public void play(@NotNull Player player) {
        Location loc = player.getLocation();
        new TotemEffectTask(plugin, duration, particleCount, loc, player, entityManager).start();
    }

    private static class TotemEffectTask extends CustomTask {
        private final Random random;
        private final Location location;
        private final long duration;
        private final int particleCount;
        private int ticksElapsed;
        private final ParticleBuilder particle;
        private final EntityManager entityManager;
        private final Vex ghostEntity;

        public TotemEffectTask(Plugin plugin, long duration, int particleCount, Location location, Player player, EntityManager entityManager) {
            super(plugin, 0, 1, true, true);
            this.duration = duration;
            this.particleCount = particleCount;
            this.location = location;
            this.entityManager = entityManager;
            this.random = new Random();

            this.particle = new ParticleBuilder(Particle.TOTEM);
            particle.allPlayers()
                    .count(2)
                    .extra(0.5)
                    .offset(0.25, 0.25, 0.25)
                    .source(player);


            Level level = ((CraftWorld)player.getWorld()).getHandle();
            this.ghostEntity = new Vex(EntityType.VEX, level);
            ghostEntity.setPos(location.getX(), location.getY() + 2, location.getZ());
            ghostEntity.setCustomNameVisible(true);
            ghostEntity.setCustomName(Component.literal(player.getName() + "'s ghost"));
            ghostEntity.setNoGravity(true);
            ghostEntity.setNoAi(true);

            Collection<Packet<?>> packets = new HashSet<>();
            packets.add(new ClientboundSetEntityDataPacket(ghostEntity.getId(), ghostEntity.getEntityData(), true));
            packets.add(new ClientboundTeleportEntityPacket(ghostEntity));
            PacketUtils.sendPackets(entityManager.getViewers(ghostEntity), packets);

            entityManager.addEntity(ghostEntity);
        }

        @Override
        public void run() {
            if (ticksElapsed >= duration) {
                entityManager.removeEntity(ghostEntity);
                cancel();
                return;
            }

            for (int i = 0; i < particleCount; i++) {
                double radius = 1.0; // Adjust the radius as needed
                double x = location.getX() + radius * (2 * random.nextDouble() - 1);
                double z = location.getZ() + radius * (2 * random.nextDouble() - 1);

                particle.location(location.getWorld(), x, location.getY(), z)
                        .spawn();
            }

            ticksElapsed++;
        }
    }
}
