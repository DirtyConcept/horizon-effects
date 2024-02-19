package net.horizonmines.effects.effects.kill;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.horizonmines.effects.effects.EffectPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Singleton
public class LightningEffect implements EffectPlayer {

    @Inject
    public LightningEffect() {}

    @Override
    public void play(@NotNull Player player) {
        player.getWorld().spigot().strikeLightningEffect(player.getLocation(), true);
    }
}
