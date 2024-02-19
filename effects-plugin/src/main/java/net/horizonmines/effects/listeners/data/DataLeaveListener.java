package net.horizonmines.effects.listeners.data;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.horizonmines.effects.data.PlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Singleton
public class DataLeaveListener implements Listener {
    private final PlayerManager playerManager;
    private final Plugin plugin;

    @Inject
    public DataLeaveListener(final @NotNull PlayerManager playerManager,
                             final @NotNull Plugin plugin) {
        this.playerManager = playerManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onLeave(final PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        boolean state = playerManager.save(uuid);
        if (!state) plugin.getSLF4JLogger().warn("Failed to save data for '" + uuid + "'");
        playerManager.unload(uuid);
    }
}
