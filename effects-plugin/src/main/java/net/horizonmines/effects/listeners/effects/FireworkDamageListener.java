package net.horizonmines.effects.listeners.effects;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@Singleton
public class FireworkDamageListener implements Listener {

    @Inject
    public FireworkDamageListener() {}

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Firework firework) {
            if (firework.hasMetadata("nodamage")) {
                event.setCancelled(true);
            }
        }
    }
}
