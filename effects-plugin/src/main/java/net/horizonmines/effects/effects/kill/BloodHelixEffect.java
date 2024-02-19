package net.horizonmines.effects.effects.kill;

import com.destroystokyo.paper.ParticleBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.horizonmines.effects.base.CustomTask;
import net.horizonmines.effects.effects.EffectPlayer;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

@Singleton
public class BloodHelixEffect implements EffectPlayer {
    private final Plugin plugin;
    private final double initialRadius;
    private final int effectAmount;
    private final double verticalStepSize;

    @Inject
    public BloodHelixEffect(Plugin plugin) {
        this.plugin = plugin;
        initialRadius = 1.3333333333333333;
        effectAmount = 10;
        verticalStepSize = 0.25;
    }

    @Override
    public void play(@NotNull Player player) {
        new BloodHelixTask(plugin, this, player).start();
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

    private static final class BloodHelixTask extends CustomTask {
        private final BloodHelixEffect effect;
        private final ParticleBuilder particle;
        private final Location center;
        private double distance;
        private double height;
        private double angleStep;

        public BloodHelixTask(@NotNull Plugin plugin, @NotNull BloodHelixEffect effect, @NotNull Player player) {
            super(plugin, 0, 2, true, true);
            this.effect = effect;
            this.distance = effect.getInitialRadius();
            this.center = player.getLocation();
            this.height = center.getY();
            this.angleStep = 0;

            // Build the particle effect.
            this.particle = new ParticleBuilder(Particle.REDSTONE);
            particle.allPlayers()
                    .count(1)
                    .color(Color.RED, 1f)
                    .extra(0)
                    .source(player);

            // Pre-calculate and cache sine and cosine values
            // todo: update pre-calculations of sine and cosine each time getEffectAmount is changed, and load all the values into static list.
            //double radian = 2 * Math.PI / effect.getEffectAmount();
        }

        @Override
        public void run() {
            int amount = effect.getEffectAmount();
            double radian = (2 * Math.PI / amount);
            for (int i = 0; i < amount; i++) {
                double cosAngle = Math.cos((radian * (i)) + ((radian / 2) * angleStep));
                double sinAngle = Math.sin((radian * (i)) + ((radian / 2) * angleStep));;
                double x = center.getX() + (cosAngle * distance);
                double z = center.getZ() + (sinAngle * distance);

                particle.location(center.getWorld(), x, height, z).spawn();
            }

            if (distance <= 0) {
                cancel();
                return;
            }

            distance -= effect.getHorizontalStepSize();
            height += effect.getVerticalStepSize();
            angleStep++;
        }
    }
}
