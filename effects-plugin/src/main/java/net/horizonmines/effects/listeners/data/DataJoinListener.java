package net.horizonmines.effects.listeners.data;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.horizonmines.effects.data.PlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;

@Singleton
public class DataJoinListener implements Listener {
    private final PlayerManager playerManager;

    @Inject
    public DataJoinListener(final @NotNull PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onJoin(final PlayerJoinEvent event) {
        playerManager.load(event.getPlayer().getUniqueId());
    }
}
