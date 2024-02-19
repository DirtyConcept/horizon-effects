package net.horizonmines.effects.utils;

import dev.sadghost.espresso.paper.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class GuiUtils {
    private static final Component BACK_ITEM_TEXT = MiniMessage.miniMessage().deserialize("<!i><red>Right-click to go back to the last page.</red></!i>");
    private static final Component CLOSE_ITEM_TEXT = MiniMessage.miniMessage().deserialize("<!i><red>Right-click to close the menu.</red></!i>");

    private GuiUtils() {}

    public static GuiItem createBackItem(final @NotNull Gui gui) {
        ItemBuilder builder = ItemBuilder.of(Material.BARRIER);
        return new GuiItem(
                builder.withName(
                        BACK_ITEM_TEXT).build(),
                (event) -> {
                    gui.open(event.getWhoClicked());
                    event.getWhoClicked().playSound(Sound.sound(Key.key("ui.button.click"), Sound.Source.MASTER, 10, 2));
                }
        );
    }

    public static GuiItem createCloseItem(final @NotNull Gui gui) {
        ItemBuilder builder = ItemBuilder.of(Material.BARRIER);
        return new GuiItem(
                builder.withName(
                        CLOSE_ITEM_TEXT).build(),
                (event) -> {
                    gui.close(event.getWhoClicked());
                    event.getWhoClicked().playSound(Sound.sound(Key.key("ui.button.click"), Sound.Source.MASTER, 10, 2));
                }
        );
    }

    public static List<String> getStringAsLore(final @Nullable String str, final int charPerLine) {
        List<String> loreLines = new ArrayList<>();

        if (charPerLine <= 0 || str == null) {
            return loreLines;
        }

        int length = str.length();
        int index = 0;

        while (index < length) {
            int endIndex = Math.min(index + charPerLine, length);

            if (endIndex < length) {
                while (endIndex > index && str.charAt(endIndex - 1) != ' ' && str.charAt(endIndex - 1) != '\n') {
                    endIndex--;
                }

                if (endIndex == index) {
                    endIndex = Math.min(index + charPerLine, length);
                }
            }

            loreLines.add(str.substring(index, endIndex));
            index = endIndex;
        }

        return loreLines;
    }



}
