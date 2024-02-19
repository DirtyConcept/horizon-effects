package net.horizonmines.effects.commands;

import jakarta.inject.Inject;
import net.horizonmines.effects.EffectsPlugin;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PluginCommandsRegisterer implements ICommandsRegisterer {
    private final EffectsPlugin effectsPlugin;
    private final List<CommandExecutor> commands;

    @Inject
    public PluginCommandsRegisterer(final @NotNull EffectsPlugin effectsPlugin,
                                    final @NotNull List<CommandExecutor> commands) {
        this.effectsPlugin = effectsPlugin;
        this.commands = commands;
    }

    @Override
    public void registerCommands() {
        commands.remove(effectsPlugin); // Remove the main class, since Plugin implements TabExecutor

        PluginCommand pluginCommand;
        for (CommandExecutor command : commands) {
            String data = command.getClass().getAnnotation(CommandData.class).name();
            pluginCommand = effectsPlugin.getCommand(data);
            if (pluginCommand != null) {
                pluginCommand.setExecutor(command);
                if (command instanceof TabCompleter tabCompleter) pluginCommand.setTabCompleter(tabCompleter);
            }
        }
    }
}
