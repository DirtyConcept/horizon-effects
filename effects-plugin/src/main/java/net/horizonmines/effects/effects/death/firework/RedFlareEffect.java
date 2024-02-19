package net.horizonmines.effects.effects.death.firework;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.horizonmines.effects.effects.death.FireworkEffect;
import org.bukkit.Color;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Singleton
public class RedFlareEffect extends FireworkEffect {
    private static final List<org.bukkit.FireworkEffect> EFFECT_LIST = List.of(
            org.bukkit.FireworkEffect.builder()
                    .withColor(Color.RED)
                    .with(org.bukkit.FireworkEffect.Type.BALL)
                    .withTrail()
                    .withFlicker()
                    .build()
    );
    private final Plugin plugin;

    @Inject
    public RedFlareEffect(final @NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected @Nullable Consumer<Entity> fireworkSetup() {
        return (entity -> {
            Firework firework = (Firework) entity;
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffects(EFFECT_LIST);
            firework.setFireworkMeta(meta);
            firework.setSilent(true);
            firework.setMetadata("nodamage", new FixedMetadataValue(plugin, true));
        });
    }
}
