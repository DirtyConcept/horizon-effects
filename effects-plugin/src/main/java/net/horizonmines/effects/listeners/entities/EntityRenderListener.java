package net.horizonmines.effects.listeners.entities;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.horizonmines.effects.entities.EntityManager;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

@Singleton
public class EntityRenderListener implements Listener {
    private final EntityManager entityManager;

    @Inject
    public EntityRenderListener(final @NotNull EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventHandler
    public void onMove(final PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) return;


        // todo: add later in scheduler maybe instead of here
        entityManager.render(((CraftPlayer) event.getPlayer()).getHandle());
    }
}
