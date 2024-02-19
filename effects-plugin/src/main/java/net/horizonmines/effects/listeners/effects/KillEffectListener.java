package net.horizonmines.effects.listeners.effects;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.horizonmines.effects.data.PlayerManager;
import net.horizonmines.effects.effects.EffectsManager;
import net.horizonmines.effects.effects.EffectsManager.EffectData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Singleton
public class KillEffectListener implements Listener {
    private final PlayerManager playerManager;
    private final EffectsManager effectsManager;

    @Inject
    public KillEffectListener(final @NotNull PlayerManager playerManager,
                              final @NotNull EffectsManager effectsManager) {
        this.playerManager = playerManager;
        this.effectsManager = effectsManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        Player killer = player.getKiller();
        if (killer == null) return;

        Optional<EffectData> data = effectsManager.getEffectData(playerManager.getData(killer.getUniqueId()).getKillEffect());
        EffectData effectData;
        if (data.isPresent() && (effectData = data.get()).isEnabled()) effectData.getEffect().play(player);
    }
}