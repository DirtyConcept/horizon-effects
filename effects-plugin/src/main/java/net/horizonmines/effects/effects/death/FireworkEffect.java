package net.horizonmines.effects.effects.death;

import net.horizonmines.effects.effects.EffectPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class FireworkEffect implements EffectPlayer {

    @Override
    public void play(@NotNull Player player) {
        Location loc = player.getLocation();
        ((Firework)player.getLocation().getWorld().spawnEntity(loc, EntityType.FIREWORK, CreatureSpawnEvent.SpawnReason.CUSTOM, fireworkSetup())).detonate();
        player.getWorld().playSound(Sound.sound(Key.key("entity.wither.break_block"), Sound.Source.MASTER, 1.2f, 2f), loc.getX(), loc.getY(), loc.getZ());
    }

    protected abstract @Nullable Consumer<Entity> fireworkSetup();
}
