package net.horizonmines.effects.effects;

import io.micronaut.context.ApplicationContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

@Singleton
public final class EffectFactory {
    private final ApplicationContext applicationContext;

    @Inject
    public EffectFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public EffectPlayer createEffect(final @NotNull EffectsManager.Effect effect) {
        return applicationContext.getBean(effect.getType());
    }
}
