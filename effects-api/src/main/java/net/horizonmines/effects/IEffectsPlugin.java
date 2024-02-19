package net.horizonmines.effects;

import io.micronaut.context.ApplicationContext;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public interface IEffectsPlugin extends Plugin {

    /**
     * Returns the plugin's Injector instance.
     *
     * @return the plugin's Injector.
     * @since 1.0.1
     */
    ApplicationContext getApplication();

    /**
     * Runs a reload action on the plugin to update values that may
     * have changes in the config.
     * <p>
     * Some features may require a restart to apply the changes to them such as
     * the MongoManager, which we do not want to reload its connection due to it
     * causing corruptions and errors.
     *
     * @since 1.0.1
     */
    void reloadPlugin();

    /**
     * Returns the plugin instance as JavaPlugin.
     *
     * @return the plugin instance as JavaPlugin.
     * @since 1.0.1
     */
    @NotNull JavaPlugin asJavaPlugin();

}
