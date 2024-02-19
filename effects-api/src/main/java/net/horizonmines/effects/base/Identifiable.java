package net.horizonmines.effects.base;

import org.jetbrains.annotations.NotNull;

/**
 * A class with identity for each object.
 * Used for POJO classes.
 */
public interface Identifiable {

    /**
     * @return the data identifier
     */
    @NotNull String getIdentifier();
}
