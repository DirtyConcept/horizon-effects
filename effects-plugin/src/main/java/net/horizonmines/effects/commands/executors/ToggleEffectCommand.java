package net.horizonmines.effects.commands.executors;

import jakarta.inject.Inject;
import net.horizonmines.effects.commands.CommandData;
import net.horizonmines.effects.effects.EffectsManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.*;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@CommandData(name = "toggleeffect")
public class ToggleEffectCommand implements TabExecutor {
    private final EffectsManager effectsManager;

    @Inject
    public ToggleEffectCommand(EffectsManager effectsManager) {
        this.effectsManager = effectsManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 2) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Usage: /toggleeffect <effect> <state></red>"));
            return true;
        }

        String arg1 = args[1];
        boolean value;
        if ("true".equalsIgnoreCase(arg1)) {
            value = true;
        } else if ("false".equalsIgnoreCase(arg1)) {
            value = false;
        } else {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>State must either be true or false.</red>"));
            return true;
        }

        boolean success;
        if (value) success = effectsManager.enableEffect(args[0]);
        else success = effectsManager.disableEffect(args[0]);

        if (!success) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You must enter an existing effect.</red>"));
            return true;
        }


        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length <= 1) {
            return StringUtil.copyPartialMatches(
                    args[0],
                    effectsManager.getEffectList().stream().map(EffectsManager.EffectData::getKey).toList(),
                    new ArrayList<>()
            );
        }

        if (args.length == 2) {
            return List.of("True", "False");
        }

        return new ArrayList<>();
    }
}
