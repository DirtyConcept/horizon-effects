package net.horizonmines.effects.data;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.horizonmines.effects.dao.MongoManager;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
public class PlayerManager {
    private final Map<UUID, PlayerCache> dataMap;
    private final MongoCollection<Document> playerCollection;

    @Inject
    public PlayerManager(final @NotNull MongoManager mongoManager) {
        this.playerCollection = mongoManager.getPlayerCollection();
        this.dataMap = new HashMap<>();
    }

    public PlayerCache getData(@NotNull UUID uuid) {
        PlayerCache data = dataMap.get(uuid);
        if (data != null) return data;

        return load(uuid);
    }

    public PlayerCache load(@NotNull UUID uuid) {
        if (dataMap.containsKey(uuid)) {
            throw new IllegalStateException("Cannot load player data when its already loaded.");
        }

        Document doc = playerCollection.find(Filters.eq("uuid", uuid.toString())).first();
        PlayerCache data;
        if (doc == null) {
            data = create(uuid);
        } else {
            data = new PlayerCache();
            data.deserialize(doc);
        }

        dataMap.put(uuid, data);
        return data;
    }

    public boolean save(@NotNull UUID uuid) {
        if (dataMap.containsKey(uuid)) {
            playerCollection.findOneAndReplace(Filters.eq("uuid", uuid.toString()), dataMap.get(uuid).serialize());
            return true;
        }

        return false;
    }

    public void unload(@NotNull UUID uuid) {
        dataMap.remove(uuid);
    }

    public PlayerCache create(@NotNull UUID uuid) {
        PlayerCache data = new PlayerCache(uuid);
        if (playerCollection.find(Filters.eq("uuid", uuid)).first() == null) {
            playerCollection.insertOne(data.serialize());
        }
        return data;
    }

    public Map<UUID, PlayerCache> getCache() {
        return dataMap;
    }
}
