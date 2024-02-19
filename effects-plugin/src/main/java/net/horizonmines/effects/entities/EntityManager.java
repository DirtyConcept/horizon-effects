package net.horizonmines.effects.entities;

import com.mojang.datafixers.util.Pair;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.horizonmines.effects.utils.PacketUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class EntityManager {
    private static final double RENDER_DISTANCE_SQUARED = 50d * 50d;
    private final Map<Entity, Set<ServerPlayer>> effectEntities;
    private final Server server;

    @Inject
    public EntityManager(final @NotNull Server server) {
        this.server = server;

        this.effectEntities = new ConcurrentHashMap<>();
    }

    public void addEntity(@NotNull Entity entity) {
        if (effectEntities.containsKey(entity)) return;

        Set<ServerPlayer> withinRender = new HashSet<>();
        for (Player player : server.getOnlinePlayers()) {
            ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
            if (isWithinRenderDistanceSquared(serverPlayer, entity)) {
                withinRender.add(serverPlayer);
                sendAddEntity(serverPlayer, entity);
            }
        }

        effectEntities.put(entity, withinRender);
    }

    public void removeEntity(@NotNull Entity entity) {
        for (ServerPlayer player : effectEntities.get(entity)) {
            sendRemoveEntities(player, new ArrayList<>(){{add(entity);}});
        }

        effectEntities.remove(entity);
    }

    public void removeEntities(final @NotNull Collection<? extends Entity> entities) {
        Map<ServerPlayer, List<Entity>> map = new HashMap<>();
        for (Entity entity : entities) {
            for (ServerPlayer player : effectEntities.get(entity)) {
                map.computeIfAbsent(player, v -> new ArrayList<>()).add(entity);
            }
        }

        for (Map.Entry<ServerPlayer, List<Entity>> entry : map.entrySet()) {
            sendRemoveEntities(entry.getKey(), entry.getValue());
        }

        for (Entity entity : entities) {
            effectEntities.remove(entity);
        }
    }

    public void render(@NotNull ServerPlayer player) {
        Set<Entity> entitiesToRemove = new HashSet<>();

        for (Entity entity : effectEntities.keySet()) {
            Set<ServerPlayer> players = effectEntities.get(entity);

            if (isWithinRenderDistanceSquared(player, entity)) {
                if (players.add(player)) {
                    sendAddEntity(player, entity);
                }
            } else if (players.contains(player)) {
                players.remove(player);
                entitiesToRemove.add(entity);
            }
        }

        sendRemoveEntities(player, entitiesToRemove);
    }

    public Set<ServerPlayer> getViewers(@NotNull Entity entity) {
        return effectEntities.get(entity);
    }

    private void sendAddEntity(final @NotNull ServerPlayer player,
                               final @NotNull Entity entity) {
        List<Packet<?>> packets = new ArrayList<>();
        packets.add(new ClientboundAddEntityPacket(entity, entity.getId()));
        packets.add(new ClientboundSetEntityDataPacket(entity.getId(), entity.getEntityData(), true));
        if (entity instanceof LivingEntity living) {
            packets.add(new ClientboundSetEquipmentPacket(
                    entity.getId(), Arrays.stream(EquipmentSlot.values()).map((slot) -> new Pair<>(slot, living.getItemBySlot(slot))).toList())
            );
        }
        PacketUtils.sendPackets(player, packets);
    }

    private void sendAddEntities(@NotNull ServerPlayer player, @NotNull List<Entity> entities) {
        for (Entity entity : entities) {
            sendAddEntity(player, entity);
        }
    }

    private void sendRemoveEntities(@NotNull ServerPlayer player, @NotNull Collection<Entity> entities) {
        int[] ids = entities.stream().mapToInt(Entity::getId).toArray();
        PacketUtils.sendPacket(player, new ClientboundRemoveEntitiesPacket(ids));
    }

    private boolean isWithinRenderDistanceSquared(@NotNull ServerPlayer player, @NotNull Entity entity) {
        double dx = player.getX() - entity.getX();
        double dy = player.getY() - entity.getY();
        double dz = player.getZ() - entity.getZ();

        return dx * dx + dy * dy + dz * dz <= RENDER_DISTANCE_SQUARED;
    }
}
