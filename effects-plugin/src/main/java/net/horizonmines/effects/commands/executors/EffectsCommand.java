package net.horizonmines.effects.commands.executors;

import jakarta.inject.Inject;
import net.horizonmines.effects.commands.CommandData;
import net.horizonmines.effects.data.PlayerCache;
import net.horizonmines.effects.data.PlayerManager;
import net.horizonmines.effects.effects.EffectsManager;
import net.horizonmines.effects.effects.EffectsManager.EffectData;
import net.horizonmines.effects.guis.EffectsCategoriesGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CommandData(name = "effects")
public class EffectsCommand implements TabExecutor {
    private final EffectsManager effectsManager;
    private final EffectsCategoriesGUI effectsCategoriesGUI;
    private final PlayerManager playerManager;

    @Inject
    public EffectsCommand(EffectsManager effectsManager, EffectsCategoriesGUI effectsCategoriesGUI, PlayerManager playerManager) {
        this.effectsManager = effectsManager;
        this.effectsCategoriesGUI = effectsCategoriesGUI;
        this.playerManager = playerManager;
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 2) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Usage: /effects <category> <effect></red>"));
            return true;
        }

        if (args.length == 0) {
            effectsCategoriesGUI.open((HumanEntity) sender);
            return true;
        }

        EffectsManager.EffectCategory category = null;
        for (EffectsManager.EffectCategory value : EffectsManager.EffectCategory.values()) {
            if (value.toString().equalsIgnoreCase(args[0])) category = value;
        }

        if (category == null) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You must enter an existing category</red>"));
            return true;
        }

        switch (args.length) {
            case 1 -> effectsCategoriesGUI.openEffectsGui((Player) sender, category);
            case 2 -> selectEffect(args[1], category, (Player) sender);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> categories;
            if (args[0].isEmpty()) {
                categories = Arrays.stream(EffectsManager.EffectCategory.values())
                        .map(Enum::toString)
                        .toList();
            } else {
                categories = Arrays.stream(EffectsManager.EffectCategory.values())
                        .map(Enum::toString)
                        .filter(string -> string.contains(args[0]))
                        .toList();
            }

            return categories;
        }

        if (args.length == 2) {
            List<String> effects;
            EffectsManager.EffectCategory category = null;
            for (EffectsManager.EffectCategory value : EffectsManager.EffectCategory.values()) {
                if (value.toString().equalsIgnoreCase(args[0])) category = value;
            }

            if (category == null) {
                effects = effectsManager.getEffectList().stream()
                        .map(EffectData::getKey)
                        .collect(Collectors.toList());
                effects.add("NONE");
                return effects;
            }

            if (args[1].isEmpty()) {
                effects = effectsManager.getEffectsByCategory(category).stream()
                        .map(EffectData::getKey)
                        .collect(Collectors.toList());
            } else {
                effects = effectsManager.getEffectsByCategory(category).stream()
                        .map(EffectData::getKey)
                        .filter(string -> string.contains(args[1]))
                        .collect(Collectors.toList());
            }

            effects.add("NONE");
            return effects;
        }

        return new ArrayList<>();
    }

    private void selectEffect(String strEffect, EffectsManager.EffectCategory category, Player player) {
        PlayerCache data = playerManager.getData(player.getUniqueId());
        if (strEffect.equalsIgnoreCase("NONE")) {
            data.setEffect(category, "NONE");
            player.sendMessage(Component.text("You have deselected your " + category.getFriendlyName() + " effect").color(NamedTextColor.RED));
            return;
        }

        Optional<EffectData> optEffect = effectsManager.getEffectData(strEffect);
        if (optEffect.isEmpty()) {
            player.sendMessage(Component.text("The effect does not exist in this category.").color(NamedTextColor.RED));
            return;
        }

        EffectData effectData = optEffect.get();
        String effectKey = data.getEffect(category);
        if (!player.hasPermission(effectData.getPermission())) {
            player.sendMessage(Component.text("You don't own this effect.").color(NamedTextColor.RED));
        } else if (!effectData.isEnabled()) {
            player.sendMessage(Component.text("This effect is currently disabled.").color(NamedTextColor.RED));
        } else if (effectKey.equals(effectData.getKey())) {
            player.sendMessage(Component.text("You already have this effect selected.").color(NamedTextColor.RED));
        } else {
            player.sendMessage(
                    Component.text("You have selected the " + effectData.getFriendlyName() + " effect.").color(NamedTextColor.GREEN)
            );
            data.setEffect(category, effectData.getKey());
        }
    }
}
