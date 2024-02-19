package net.horizonmines.effects;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Singleton
public final class EffectsAPI implements IEffectsAPI {
    @NotNull private final IEffectsPlugin plugin;

    @Inject
    @Contract(pure = true)
    public EffectsAPI(final @NotNull IEffectsPlugin plugin) {
        this.plugin = plugin;
    }
}
