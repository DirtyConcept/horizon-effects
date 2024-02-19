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

import java.awt.*;
import java.util.Random;

@Singleton
public class RainbowPotionEffect implements EffectPlayer {
    private final Plugin plugin;

    @Inject
    public RainbowPotionEffect(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void play(@NotNull Player player) {
        new RainbowPotionEffectTask(plugin, player).start();
    }

    private static class RainbowPotionEffectTask extends CustomTask {
        private final Random random;
        private final Location location;
        private int ticksElapsed;
        private final ParticleBuilder particle;


        public RainbowPotionEffectTask(Plugin plugin, Player player) {
            super(plugin, 0, 1, true, true);
            this.location = player.getLocation();
            this.ticksElapsed = 0;
            this.random = new Random();

            this.particle = new ParticleBuilder(Particle.SPELL_MOB)
                    .source(player)
                    .extra(1)
                    .count(0);
        }

        @Override
        public void run() {
            if (ticksElapsed >= 160) {
                this.cancel();
                return;
            }

            spawnRainbowParticle(location.getX(), location.getY(), location.getZ());
            ticksElapsed++;
        }

        private void spawnRainbowParticle(double x, double y, double z) {
            for (int i = 0; i < 10; i++) {
                float hue = random.nextFloat(); // Random hue value between 0.0 and 1.0
                Color color = Color.getHSBColor(hue, 1, 1);
                particle.location(location.getWorld(), x, y, z)
                        .offset(color.getRed() / 255d, color.getGreen() / 255d, color.getBlue() / 255d)
                        .spawn();
            }
        }
    }
}
