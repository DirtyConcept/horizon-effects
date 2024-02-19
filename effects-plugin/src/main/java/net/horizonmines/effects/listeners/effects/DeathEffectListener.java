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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

@Singleton
public class DeathEffectListener implements Listener {
    private static final Set<EntityDamageEvent.DamageCause> ALLOWED_CAUSES = Set.of(
            EntityDamageEvent.DamageCause.ENTITY_ATTACK,
            EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK,
            EntityDamageEvent.DamageCause.PROJECTILE,
            EntityDamageEvent.DamageCause.LIGHTNING,
            EntityDamageEvent.DamageCause.HOT_FLOOR,
            EntityDamageEvent.DamageCause.THORNS
    );
    private final PlayerManager playerManager;
    private final EffectsManager effectsManager;

    @Inject
    public DeathEffectListener(final @NotNull PlayerManager playerManager,
                               final @NotNull EffectsManager effectsManager) {
        this.playerManager = playerManager;
        this.effectsManager = effectsManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        EntityDamageEvent damageEvent = event.getEntity().getLastDamageCause();
        if (damageEvent == null || !ALLOWED_CAUSES.contains(damageEvent.getCause())) return;

        Optional<EffectData> data = effectsManager.getEffectData(playerManager.getData(player.getUniqueId()).getDeathEffect());
        EffectData effectData;
        if (data.isPresent() && (effectData = data.get()).isEnabled()) effectData.getEffect().play(player);
    }
}