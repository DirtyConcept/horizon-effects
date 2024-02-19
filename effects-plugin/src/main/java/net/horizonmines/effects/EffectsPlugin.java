package net.horizonmines.effects;

import io.micronaut.context.ApplicationContext;
import net.horizonmines.effects.commands.ICommandsRegisterer;
import net.horizonmines.effects.data.PlayerManager;
import net.horizonmines.effects.listeners.IListenerRegisterer;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion.Target;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import org.bukkit.plugin.java.annotation.plugin.author.Authors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Plugin(name = "horizon-effects", version = "1.0.0")
@ApiVersion(Target.v1_19)
@Authors(@Author("SadGhost/DirtyConcepts"))
@Commands({
        @Command(name = "toggleeffect", permission = "effects.command.toggle"),
        @Command(name = "effects", aliases = {"horizoneffects"})
})
public final class EffectsPlugin extends JavaPlugin implements IEffectsPlugin {
    private ApplicationContext applicationContext;

    @Override
    public void onLoad() {

        applicationContext = ApplicationContext.builder()
                .classLoader(getClassLoader())
                .singletons(this)
                .build();
    }

    @Override
    public void onEnable() {
        applicationContext.start();

        final IListenerRegisterer listenerRegisterer = applicationContext.getBean(IListenerRegisterer.class);
        listenerRegisterer.registerListeners();

        final ICommandsRegisterer commandsRegisterer = applicationContext.getBean(ICommandsRegisterer.class);
        commandsRegisterer.registerCommands();

        getServer().getServicesManager().register(IEffectsAPI.class, applicationContext.getBean(IEffectsAPI.class), this, ServicePriority.Highest);
    }

    @Override
    public void onDisable() {
        // Maybe remove data saving and make it just set the changes on the Mongo?
        PlayerManager playerManager = applicationContext.getBean(PlayerManager.class);
        for (UUID uuid : playerManager.getCache().keySet()) {
            boolean state = playerManager.save(uuid);
            if (!state) getSLF4JLogger().warn("Failed to save data for '" + uuid + "'");
        }

        getServer().getScheduler().cancelTasks(this);

        applicationContext.stop();
    }

    @Override
    public ApplicationContext getApplication() {
        return applicationContext;
    }

    @Override
    @Contract(pure = true)
    public void reloadPlugin() {}

    @Override
    @Contract(value = " -> this", pure = true)
    public @NotNull JavaPlugin asJavaPlugin() {
        return this;
    }

    public void registerCommands() {

    }
}
