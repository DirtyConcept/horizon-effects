package net.horizonmines.effects.guis;

import dev.sadghost.espresso.groups.Pair;
import dev.sadghost.espresso.paper.ItemBuilder;
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.horizonmines.effects.data.PlayerCache;
import net.horizonmines.effects.data.PlayerManager;
import net.horizonmines.effects.effects.EffectsManager;
import net.horizonmines.effects.effects.EffectsManager.EffectCategory;
import net.horizonmines.effects.effects.EffectsManager.EffectData;
import net.horizonmines.effects.utils.GuiUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EffectsListGUI extends Gui {
    private final HumanEntity player;
    private final EffectCategory effectType;
    private final EffectsManager effectsManager;
    private final PlayerManager playerManager;

    private Pair<Integer, EffectData> selectedSlot;

    public EffectsListGUI(final @NotNull HumanEntity player,
                          final @NotNull EffectsCategoriesGUI parent,
                          final @NotNull EffectsManager.EffectCategory effectType,
                          final @NotNull EffectsManager effectsManager,
                          final @NotNull PlayerManager playerManager) {
        super(calculateSlots(
                effectsManager.getEffectList().size()),
                "Death Effects",
                InteractionModifier.VALUES
        );
        this.player = player;
        this.effectType = effectType;
        this.effectsManager = effectsManager;
        this.playerManager = playerManager;

        ItemStack bgItem = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
                .withName(Component.text(""))
                .build();
        getFiller().fill(new GuiItem(bgItem));

        loadEffects();
        setItem(18, GuiUtils.createBackItem(parent));
    }

    public void loadEffects() {
        List<EffectData> effects = effectsManager.getEffectsByCategory(effectType);
        effects.add(0, EffectsManager.EMPTY_EFFECT);

        int j = 1;
        int i = calcAdjustment(effects.size());
        int middleSpace = 0;

        for (int k = effects.size() - 1; k >= 0; k--) {
            EffectData effect = effects.get(k);

            if (i == 4 && middleSpace > 0) {
                // make sure that middle slots would be even in case
                // of effects number being even (0XXX0XXX0 where X is effect item & 0 is fill slot)
                i += middleSpace;
            } else if (i >= 8) {
                j++;
                // make centered spacing from the borders of the gui.
                // for example, 8 items means 1 row has 7 with 1 space and 1 row has ((7 - 1) / 2) + 1 = 4 empty spaces from each side.
                i = calcAdjustment(k + 1);
                if (i % 2 == 0) middleSpace = 1;
            }

            int slot = i + (j * 9);
            if (slot >= getInventory().getSize()) {
                throw new IllegalStateException("Too many effects within the gui.");
            }

            setItem(slot, buildEffectItem(effect, slot));
            i++;
        }
    }

    private int calcAdjustment(int size) {
        return size > 7 ? 1 : ((7 - size) / 2) + 1;
    }

    public GuiItem buildEffectItem(EffectData effectData, final int slot) {
        PlayerCache data = playerManager.getData(player.getUniqueId());
        if (effectData.getKey().equalsIgnoreCase("NONE")) {
            GuiItem resetItem = new GuiItem(
                    ItemBuilder.of(Material.BARRIER)
                            .withName(Component.text("Reset"))
                            .build()
            );

            resetItem.setAction(event -> {
                event.getWhoClicked().sendMessage(Component.text("You have reset your " + effectType.getFriendlyName() + " effect").color(NamedTextColor.GREEN));
                data.setEffect(effectType, effectData.getKey());
                updateEffectItem(effectData, event.getSlot());
                player.playSound(Sound.sound(Key.key("entity.item.pickup"), Sound.Source.MASTER, 10, 2));
            });

            return resetItem;
        } else {

            List<Component> description = new ArrayList<>();
            description.add(Component.empty());

            MiniMessage miniMessage = MiniMessage.miniMessage();
            for (String str : GuiUtils.getStringAsLore(effectData.getDescription(), 50)) {
                description.add(miniMessage.deserialize(str));
            }
            if (description.size() > 1) description.add(Component.empty());

            String effectKey = data.getEffect(effectType);
            GuiAction<InventoryClickEvent> action;
            Component actionMessage;

            ItemBuilder builder = ItemBuilder.of(effectData.getDisplay())
                    .withName(Component.text(effectData.getFriendlyName())
                            .style(Style.style().decoration(TextDecoration.ITALIC, false).build())
                            .color(NamedTextColor.GRAY)
                    )
                    .addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_ENCHANTS);

            if (!player.hasPermission(effectData.getPermission())) {
                action = (event) -> {
                    event.getWhoClicked().sendMessage(Component.text("You don't own this effect.").color(NamedTextColor.RED));
                    player.playSound(Sound.sound(Key.key("block.wooden_button.click_on"), Sound.Source.MASTER, 10, 2));
                };
                actionMessage = Component.text("Locked")
                        .style(Style.style().color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false).build());
            } else if (!effectData.isEnabled()) {
                action = (event) -> {
                    event.getWhoClicked().sendMessage(Component.text("This effect is currently disabled.").color(NamedTextColor.RED));
                    player.playSound(Sound.sound(Key.key("block.wooden_button.click_on"), Sound.Source.MASTER, 10, 2));
                };
                actionMessage = Component.text("Unavailable")
                        .style(Style.style().color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false).build());
            } else if (effectKey.equals(effectData.getKey()) && !effectData.getKey().equals("NONE")) {
                action = (event) -> {
                    event.getWhoClicked().sendMessage(Component.text("You already have this effect selected.").color(NamedTextColor.RED));
                    player.playSound(Sound.sound(Key.key("block.wooden_button.click_on"), Sound.Source.MASTER, 10, 2));
                };
                actionMessage = Component.text("Selected")
                        .style(Style.style().color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false).build());
                selectedSlot = new Pair<>(slot, effectData);
                builder.addEnchant(Enchantment.DURABILITY, 0);
            } else {
                action = (event) -> {
                    event.getWhoClicked().sendMessage(
                            Component.text("You have selected the " + effectData.getFriendlyName() + " effect.").color(NamedTextColor.GREEN)
                    );
                    data.setEffect(effectType, effectData.getKey());
                    updateEffectItem(effectData, event.getSlot());
                    player.playSound(Sound.sound(Key.key("entity.item.pickup"), Sound.Source.MASTER, 10, 2));
                };
                actionMessage = Component.text("Click to select")
                        .style(Style.style().color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false).build());
                ;
            }

            description.add(actionMessage);
            GuiItem item = new GuiItem(
                    builder.withLore(description)
                            .build()
            );
            item.setAction(action);
            return item;
        }
    }

    private void updateEffectItem(EffectData data, int slot) {
        if (selectedSlot != null) {
            updateItem(selectedSlot.getA(), buildEffectItem(selectedSlot.getB(), selectedSlot.getA()));
        }

        updateItem(slot, buildEffectItem(data, slot));
        selectedSlot = new Pair<>(slot, data);
    }

    private static int calculateSlots(int size) {
        // add 1 for the none slot and 6 to find out if another row is needed. rounds to the slot up if the number
        int items = size + 7;
        return 2 + (items) / 7;
    }
}
