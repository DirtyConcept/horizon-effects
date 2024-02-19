package net.horizonmines.effects.guis;

import dev.sadghost.espresso.paper.ItemBuilder;
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.horizonmines.effects.data.PlayerManager;
import net.horizonmines.effects.effects.EffectsManager;
import net.horizonmines.effects.effects.EffectsManager.EffectCategory;
import net.horizonmines.effects.utils.GuiUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class EffectsCategoriesGUI extends Gui {
    private final EffectsManager effectsManager;
    private final PlayerManager playerManager;

    @Inject
    public EffectsCategoriesGUI(final @NotNull EffectsManager effectsManager,
                                final @NotNull PlayerManager playerManager) {
        super(3, "Effects", InteractionModifier.VALUES);
        this.effectsManager = effectsManager;
        this.playerManager = playerManager;
        Map<Integer, EffectCategory> categoryMap = new HashMap<>();

        ItemStack bgItem = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
                .withName(Component.text(""))
                .build();

        getFiller().fill(new GuiItem(bgItem));

        categoryMap.put(10, EffectCategory.DEATH);
        categoryMap.put(12, EffectCategory.KILL);
        for (Map.Entry<Integer, EffectCategory> entry : categoryMap.entrySet()) {
            EffectCategory category = entry.getValue();
            setItem(
                    entry.getKey(),
                    new GuiItem(
                            ItemBuilder.of(category.getDisplay())
                                    .withName(MiniMessage.miniMessage().deserialize("<!i><gray>" + EffectCategory.DEATH.getFriendlyName() + " Effects</gray></!i>"))
                                    .withLore(
                                            Component.empty(),
                                            MiniMessage.miniMessage().deserialize("<!i><yellow>Click to view</yellow></!i>")
                                    )
                                    .addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                                    .build(),
                            (event) -> {
                                openEffectsGui(event.getWhoClicked(), category);
                                event.getWhoClicked().playSound(Sound.sound(Key.key("ui.button.click"), Sound.Source.MASTER, 10, 2));
                            }
                    )
            );
        }

        setItem(18, GuiUtils.createCloseItem(this));
    }

    private EffectsListGUI createEffectGui(final @NotNull HumanEntity entity,
                                           final @NotNull EffectsCategoriesGUI effectsGUI,
                                           final @NotNull EffectCategory effectType) {
        return new EffectsListGUI(entity, effectsGUI, effectType, effectsManager, playerManager);
    }

    public void openEffectsGui(final @NotNull HumanEntity entity,
                                final @NotNull EffectCategory effectType) {
        createEffectGui(entity, this, effectType).open(entity);
    }
}
