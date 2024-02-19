package net.horizonmines.effects.effects.death;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.horizonmines.effects.base.CustomTask;
import net.horizonmines.effects.effects.EffectPlayer;
import net.horizonmines.effects.entities.OptimizedStand;
import net.horizonmines.effects.entities.EntityManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minecraft.core.Rotations;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Singleton
public class TombstoneEffect implements EffectPlayer {
    private final Plugin plugin;
    private final EntityManager entityManager;

    @Inject
    public TombstoneEffect(final @NotNull Plugin plugin,
                           final @NotNull EntityManager entityManager) {
        this.plugin = plugin;
        this.entityManager = entityManager;
    }

    @Override
    public void play(@NotNull Player player) {
        final Location loc = player.getLocation();
        player.getWorld().playSound(
                Sound.sound(Key.key("entity.skeleton.death"), Sound.Source.MASTER, 10f, 0.5f),
                loc.getX(), loc.getY(), loc.getZ()
        );

        new TombstoneTask(plugin, entityManager, player).start();
    }

    private static final class TombstoneTask extends CustomTask {
        private static final TombStructureData[] TOMB_STRUCTURE_DATA = createTombStructureData();
        private final EntityManager entityManager;

        private final Location center;
        private final List<OptimizedStand> armorStands;

        public TombstoneTask(final @NotNull Plugin plugin,
                             final @NotNull EntityManager entityManager,
                             final @NotNull Player player) {
            super(plugin, 160L, 0, false, true);
            this.entityManager = entityManager;
            this.center = player.getLocation();
            this.armorStands = new ArrayList<>();

            spawnTombstone();
        }

        @Override
        public void run() {
            removeTombstone();
            center.getWorld().playSound(
                    Sound.sound(Key.key("entity.stray.death"), Sound.Source.MASTER, 10f, 1.75f),
                    center.getX(), center.getY(), center.getZ()
            );
        }

        private void spawnTombstone() {
            CraftWorld craftWorld = (CraftWorld) center.getWorld();
            Level level = craftWorld.getHandle();

            double rotation = Math.toRadians(center.getYaw() + 90);
            // Calculate the safe Y location
            Location safePos = new Location(center.getWorld(), center.getX(), center.getBlockY() + 1, center.getZ());
            while (safePos.getBlock().isPassable()) {
                safePos.subtract(0, 1, 0);
            }

            safePos.getBlock().getBoundingBox().getHeight();

            for (TombStructureData data : TOMB_STRUCTURE_DATA) {
                OptimizedStand armorStand = new OptimizedStand(EntityType.ARMOR_STAND, level);
                armorStand.setSmall(true);
                armorStand.setInvisible(true);
                armorStand.setSilent(true);
                armorStand.setNoGravity(true);
                armorStand.setHeadPose(new Rotations(0, center.getYaw() + 90, 0));
                // Local offsets from the center
                double localX = data.getX();
                double localZ = data.getZ();

                // Adjust local XZ based on player's look direction
                double rotatedX = (localX * Math.cos(rotation) - localZ * Math.sin(rotation)) * 0.4385d;
                double rotatedZ = (localX * Math.sin(rotation) + localZ * Math.cos(rotation)) * 0.4385d;

                armorStand.setRot(0, 0);
                armorStand.setPos(center.getX() + rotatedX, safePos.getY() + (data.getY() * 0.4385d), center.getZ() + rotatedZ);
                data.getConsumer().accept(armorStand);

                armorStands.add(armorStand);
                entityManager.addEntity(armorStand);
            }
        }

        private void removeTombstone() {
            entityManager.removeEntities(armorStands);
        }

        private static TombStructureData[] createTombStructureData() {
            return new TombStructureData[] {
                    createData(0, 0, 0, stand -> stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DARK_OAK_PLANKS))),
                    createData(1, 0, 0, stand -> stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DARK_OAK_PLANKS))),
                    createData(-1, 0, 0, stand -> stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DARK_OAK_PLANKS))),
                    createData(-1, 1, 0, stand -> stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.COBBLESTONE))),
                    createData(-1, 2, -1, stand -> stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.COBBLESTONE))),
                    createData(-1, 2, 0, stand -> stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.COBBLESTONE))),
                    createData(-1, 2, 1, stand -> stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.COBBLESTONE))),
                    createData(-1, 3, 0, stand -> stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.COBBLESTONE)))
            };
        }

        private static TombStructureData createData(double x, double y, double z, Consumer<OptimizedStand> consumer) {
            return new TombStructureData(x, y, z, consumer);
        }
    }

    private static final class TombStructureData {
        private final double x, y, z;
        private final Consumer<OptimizedStand> consumer;

        private TombStructureData(final double x,
                                  final double y,
                                  final double z,
                                  final @NotNull Consumer<OptimizedStand> consumer) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.consumer = consumer;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }

        public @NotNull Consumer<OptimizedStand> getConsumer() {
            return consumer;
        }
    }
}
