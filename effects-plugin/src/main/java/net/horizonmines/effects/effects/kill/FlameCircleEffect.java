package net.horizonmines.effects.effects.kill;

import com.destroystokyo.paper.ParticleBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.horizonmines.effects.base.CustomTask;
import net.horizonmines.effects.effects.EffectPlayer;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

@Singleton
public class FlameCircleEffect implements EffectPlayer {
    private final Plugin plugin;
    private final double initialRadius;
    private final int effectAmount;
    private final double verticalStepSize;
    private final double maxHeight;

    @Inject
    public FlameCircleEffect(Plugin plugin) {
        this.plugin = plugin;
        initialRadius = 2d;
        effectAmount = 20;
        verticalStepSize = 0.25d;
        maxHeight = 2.5d;
    }

    @Override
    public void play(@NotNull Player player) {
        new FlameCircleTask(plugin, this, player).start();
    }

    public double getInitialRadius() {
        return initialRadius;
    }

    public double getHorizontalStepSize() {
        return initialRadius / effectAmount;
    }

    public int getEffectAmount() {
        return effectAmount;
    }

    public double getVerticalStepSize() {
        return verticalStepSize;
    }

    public double getMaxHeight() {
        return maxHeight;
    }

    private static final class FlameCircleTask extends CustomTask {
        private final FlameCircleEffect effect;
        private final ParticleBuilder particle;
        private final Location center;
        private final double distance;
        private double height;
        private final double maxHeight;

        public FlameCircleTask(@NotNull Plugin plugin, @NotNull FlameCircleEffect effect, @NotNull Player player) {
            super(plugin, 0, 2, true, true);
            this.effect = effect;
            this.distance = effect.getInitialRadius();
            this.center = player.getLocation();
            this.height = center.getY();
            this.maxHeight = center.getY() + effect.getMaxHeight();

            // Build the particle effect.
            this.particle = new ParticleBuilder(Particle.FLAME);
            particle.allPlayers()
                    .count(1)
                    .extra(0)
                    .source(player);

            // todo: maybe use in the future if sine cosine caching will be required
            //double radian = 2 * Math.PI / effect.getEffectAmount();
        }

        @Override
        public void run() {
            int amount = effect.getEffectAmount();
            double radian = (2 * Math.PI / amount);
            for (int i = 0; i < amount; i++) {
                double cosAngle = Math.cos(radian * (i));
                double sinAngle = Math.sin(radian * (i));;
                double x = center.getX() + (cosAngle * distance);
                double z = center.getZ() + (sinAngle * distance);

                particle.location(center.getWorld(), x, height, z).spawn();
            }

            if (height >= maxHeight) {
                cancel();
                return;
            }

            height += effect.getVerticalStepSize();
        }
    }
}

