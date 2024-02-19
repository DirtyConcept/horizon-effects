package net.horizonmines.effects.effects;

import dev.sadghost.espresso.spigot.files.FileManager;
import dev.sadghost.espresso.spigot.files.IConfig;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.horizonmines.effects.effects.death.BeheadedEffect;
import net.horizonmines.effects.effects.death.GhostsEffect;
import net.horizonmines.effects.effects.death.TombstoneEffect;
import net.horizonmines.effects.effects.death.TotemEffect;
import net.horizonmines.effects.effects.death.firework.BlueBurstEffect;
import net.horizonmines.effects.effects.death.firework.RedFlareEffect;
import net.horizonmines.effects.effects.death.firework.YellowStarEffect;
import net.horizonmines.effects.effects.kill.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("UnstableApiUsage")
@Singleton
public class EffectsManager {
    public static final EffectData EMPTY_EFFECT = new EffectData("NONE", null, "effects.reset", null, Material.BARRIER, "None", "");
    private final List<EffectData> effectMap;

    private final EffectFactory effectFactory;
    private final FileManager fileManager;

    @Inject
    public EffectsManager(final @NotNull EffectFactory effectFactory,
                          final @NotNull FileManager fileManager) {
        this.effectFactory = effectFactory;
        this.fileManager = fileManager;
        this.effectMap = new CopyOnWriteArrayList<>();

        init();
    }

    public void register(final @NotNull EffectData effect) {
        effectMap.add(effect);
    }

    public Optional<EffectPlayer> getEffect(final @NotNull String key) {
        Optional<EffectData> data = getEffectData(key);
        return data.map(EffectData::getEffect);
    }

    public @NotNull Optional<EffectData> getEffectData(final @NotNull String key) {
        return effectMap.stream().filter(effectData -> Objects.equals(effectData.getKey(), key)).findFirst();
    }

    public boolean disableEffect(final @NotNull String key) {
        Optional<EffectData> data = getEffectData(key);
        if (data.isEmpty()) return false;
        data.get().setEnabled(false);

        IConfig config = fileManager.getConfig("config.yml");
        List<String> disabledEffects = config.getStringList("general.disabled-effects");
        if (!disabledEffects.contains(key)) { // Prevent duplicate values
            disabledEffects.add(key);
            config.set("general.disabled-effects", disabledEffects);
            config.saveConfig();
        }
        return true;
    }

    public boolean enableEffect(final @NotNull String key) {
        Optional<EffectData> data = getEffectData(key);
        if (data.isEmpty()) return false;
        data.get().setEnabled(true);

        IConfig config = fileManager.getConfig("config.yml");
        List<String> disabledEffects = config.getStringList("general.disabled-effects");
        disabledEffects.remove(key);
        config.set("general.disabled-effects", disabledEffects);
        config.saveConfig();
        return true;
    }

    public List<EffectData> getEffectList() {
        return effectMap;
    }

    public List<EffectData> getEffectsByCategory(EffectCategory effectCategory) {
        List<EffectData> dataList = new ArrayList<>();
        for (EffectData effectData : effectMap) {
            if (effectData.getType() == effectCategory) {
                dataList.add(effectData);
            }
        }

        return dataList;
    }

    public void init() {
        final IConfig config = fileManager.getConfig("config.yml");
        final List<String> disabledEffects = config.getStringList("general.disabled-effects");
        for (EffectCategory category : EffectCategory.values()) {
            for (Effect effect : category.getEffects()) {
                final String permission = config.getString(
                        category.toString().toLowerCase() + "."
                                + effect.toString().toLowerCase().replaceAll("_", "-")
                                + ".permission"
                );

                EffectData data = new EffectData(
                        effect.toString(),
                        effectFactory.createEffect(effect),
                        permission,
                        category,
                        effect.getDisplay(),
                        effect.getFriendlyName(),
                        effect.getDescription(),
                        !disabledEffects.contains(effect.toString()));
                Bukkit.getLogger().info(data.toString());
                register(data);
            }
        }
    }

    public static final class EffectData {
        private final String key;
        private final EffectPlayer effect;
        private final String permission;
        private final EffectCategory type;
        @NotNull private final Material display;
        @NotNull private final String friendlyName;
        @Nullable private final String description;
        private boolean enabled;

        public EffectData(final @NotNull String key,
                          final @Nullable EffectPlayer effect,
                          final @Nullable String permission,
                          final @Nullable EffectCategory type,
                          final @NotNull Material display,
                          final @NotNull String friendlyName,
                          final @Nullable String description) {
            this.key = key;
            this.effect = effect;
            this.permission = permission;
            this.type = type;
            this.display = display;
            this.friendlyName = friendlyName;
            this.description = description;
            // Maybe change this in the future
            this.enabled = true;
        }

        public EffectData(final @NotNull String key,
                          final @NotNull EffectPlayer effect,
                          final @Nullable String permission,
                          final @NotNull EffectCategory type,
                          final @NotNull Material display,
                          final @NotNull String friendlyName,
                          final @Nullable String description,
                          final boolean enabled) {
            this.key = key;
            this.effect = effect;
            this.permission = permission;
            this.type = type;
            this.display = display;
            this.friendlyName = friendlyName;
            this.description = description;
            // Maybe change this in the future
            this.enabled = enabled;
        }

        public String getKey() {
            return key;
        }

        public EffectPlayer getEffect() {
            return effect;
        }

        public String getPermission() {
            return permission;
        }

        public EffectCategory getType() {
            return type;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public @NotNull Material getDisplay() {
            return display;
        }

        public @NotNull String getFriendlyName() {
            return friendlyName;
        }

        public @Nullable String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return "EffectData{" +
                    "key='" + key + '\'' +
                    ", effect=" + effect +
                    ", permission='" + permission + '\'' +
                    ", type=" + type +
                    ", display=" + display +
                    ", friendlyName='" + friendlyName + '\'' +
                    ", description='" + description + '\'' +
                    ", enabled=" + enabled +
                    '}';
        }
    }

    public enum EffectCategory {
        DEATH(
                Material.ZOMBIE_HEAD,
                "Death",
                List.of(
                        Effect.BLUE_BURST,
                        Effect.RED_FLARE,
                        Effect.YELLOW_STAR,
                        Effect.BEHEADED,
                        Effect.GHOSTS,
                        Effect.TOMBSTONE,
                        Effect.TOTEM
                )
        ),

        KILL(
                Material.IRON_SWORD,
                "Kill",
                List.of(
                        Effect.BLOOD_HELIX,
                        Effect.EXPLOSION,
                        Effect.FLAME_CIRCLE,
                        Effect.LIGHTNING,
                        Effect.RAINBOW_POTION
                )
        );

        private final Material display;
        private final String friendlyName;
        private final List<Effect> effects;

        EffectCategory(final @NotNull Material display,
                       final @NotNull String friendlyName,
                       final @NotNull List<Effect> effects) {
            this.display = display;
            this.friendlyName = friendlyName;
            this.effects = effects;
        }

        public Material getDisplay() {
            return display;
        }

        public String getFriendlyName() {
            return friendlyName;
        }

        public @NotNull List<Effect> getEffects() {
            return effects;
        }
    }

    public enum Effect {
        // Death
        BLUE_BURST(Material.FIREWORK_ROCKET, "Blue Burst Firework", null, BlueBurstEffect.class),
        RED_FLARE(Material.FIREWORK_ROCKET, "Red Flare Firework", null, RedFlareEffect.class),
        YELLOW_STAR(Material.FIREWORK_ROCKET, "Yellow Star Firework", null, YellowStarEffect.class),
        BEHEADED(Material.REDSTONE, "Beheading", null, BeheadedEffect.class),
        GHOSTS(Material.SOUL_CAMPFIRE, "Haunting Ghosts", null, GhostsEffect.class),
        TOMBSTONE(Material.DEAD_BUSH, "R.I.P", null, TombstoneEffect.class),
        TOTEM(Material.TOTEM_OF_UNDYING, "Totem", null, TotemEffect.class),

        // Kill,
        BLOOD_HELIX(Material.REDSTONE, "Blood Helix", null, BloodHelixEffect.class),
        EXPLOSION(Material.TNT, "KABOOM!", null, ExplosionEffect.class),
        FLAME_CIRCLE(Material.CAMPFIRE, "Flame Circle", null, FlameCircleEffect.class),
        LIGHTNING(Material.TRIDENT, "Lightning", null, LightningEffect.class),
        RAINBOW_POTION(Material.SPLASH_POTION, "Rainbow Splash", null, RainbowPotionEffect.class)
        ;

        @NotNull private final Material display;
        @NotNull private final String friendlyName;
        @Nullable private final String description;
        @NotNull private final Class<? extends EffectPlayer> type;

        @Contract(pure = true)
        Effect(final @NotNull Material display,
               final @NotNull String friendlyName,
               final @Nullable String description,
               final @NotNull Class<? extends EffectPlayer> type) {
            this.display = display;
            this.friendlyName = friendlyName;
            this.description = description;
            this.type = type;
        }

        public @NotNull Material getDisplay() {
            return display;
        }

        public @NotNull String getFriendlyName() {
            return friendlyName;
        }

        public @Nullable String getDescription() {
            return description;
        }

        public @NotNull Class<? extends EffectPlayer> getType() {
            return type;
        }
    }
}
