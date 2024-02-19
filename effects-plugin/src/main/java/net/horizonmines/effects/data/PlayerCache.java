package net.horizonmines.effects.data;

import dev.sadghost.espresso.database.IDocumentSerializable;
import net.horizonmines.effects.effects.EffectsManager;
import org.bson.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerCache implements IDocumentSerializable<Document> {
    private UUID uuid;
    private final Map<EffectsManager.EffectCategory, String> effects;

    public PlayerCache() {
        this.effects = new HashMap<>();
        effects.put(EffectsManager.EffectCategory.KILL, "NONE");
        effects.put(EffectsManager.EffectCategory.DEATH, "NONE");
    }

    public PlayerCache(UUID uuid) {
        this();
        this.uuid = uuid;
    }

    public String getEffect(EffectsManager.EffectCategory category) {
        return effects.get(category);
    }

    public void setEffect(EffectsManager.EffectCategory category, String value) {
        effects.put(category, value);
    }

    public String getKillEffect() {
        return effects.get(EffectsManager.EffectCategory.KILL);
    }

    public void setKillEffect(String killEffect) {
        effects.put(EffectsManager.EffectCategory.KILL, killEffect);
    }

    public String getDeathEffect() {
        return effects.get(EffectsManager.EffectCategory.DEATH);
    }

    public void setDeathEffect(String deathEffect) {
        effects.put(EffectsManager.EffectCategory.DEATH, deathEffect);
    }

    @Override
    public void deserialize(Document document) {
        uuid = UUID.fromString(document.getString("uuid"));
        effects.put(EffectsManager.EffectCategory.KILL, (String) document.getOrDefault("kill-effect", "none"));
        effects.put(EffectsManager.EffectCategory.DEATH, (String) document.getOrDefault("death-effect", "none"));
    }

    @Override
    public Document serialize() {
        Document document = new Document();
        document.put("uuid", uuid.toString());
        document.put("kill-effect", effects.getOrDefault(EffectsManager.EffectCategory.KILL, "none"));
        document.put("death-effect", effects.getOrDefault(EffectsManager.EffectCategory.DEATH, "none"));
        return document;
    }
}
