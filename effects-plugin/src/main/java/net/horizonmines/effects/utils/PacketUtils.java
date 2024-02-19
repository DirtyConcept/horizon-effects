package net.horizonmines.effects.utils;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class PacketUtils {

    private PacketUtils() {}

    public static void sendAllPacket(final @NotNull World level,
                                     final @NotNull Packet<?> packet) {
        for (Player player : level.getPlayers()) {
            sendPacket(player, packet);
        }
    }

    public static void sendAllPackets(final @NotNull World level,
                                     final @NotNull Packet<?>... packet) {
        for (Player player : level.getPlayers()) {
            sendPackets(player, packet);
        }
    }

    public static void sendAllPackets(final @NotNull World level,
                                      final @NotNull Collection<Packet<?>> packets) {
        for (Player player : level.getPlayers()) {
            sendPackets(player, packets);
        }
    }

    public static void sendPacketInRange(final @NotNull Location loc,
                                         final int range,
                                         final @NotNull Packet<?>... packet) {
        for (Player player : loc.getNearbyPlayers(range)) {
            sendPackets(player, packet);
        }
    }

    public static void sendPacket(final @NotNull Player player,
                                  final @NotNull Packet<?> packet) {
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    public static void sendPacket(final @NotNull ServerPlayer player,
                                  final @NotNull Packet<?> packet) {
        player.connection.send(packet);
    }

    public static void sendPackets(final @NotNull Player player,
                                  final @NotNull Packet<?>... packets) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        for (Packet<?> packet : packets) {
            connection.send(packet);
        }
    }

    public static void sendPackets(final @NotNull Collection<ServerPlayer> players,
                                   final @NotNull Packet<?>... packets) {
        for (ServerPlayer player : players) {
            for (Packet<?> packet : packets) {
                player.connection.send(packet);
            }
        }
    }

    public static void sendPackets(final @NotNull Collection<ServerPlayer> players,
                                   final @NotNull Collection<Packet<?>> packets) {
        for (ServerPlayer player : players) {
            for (Packet<?> packet : packets) {
                player.connection.send(packet);
            }
        }
    }


    public static void sendPackets(final @NotNull ServerPlayer player,
                                   final @NotNull Collection<Packet<?>> packets) {
        for (Packet<?> packet : packets) {
            player.connection.send(packet);
        }
    }

    public static void sendPackets(final @NotNull Player player,
                                   final @NotNull Collection<Packet<?>> packets) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        for (Packet<?> packet : packets) {
            connection.send(packet);
        }
    }
}
