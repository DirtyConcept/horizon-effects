package net.horizonmines.effects.dao;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import dev.sadghost.espresso.base.Preconditions;
import dev.sadghost.espresso.spigot.files.FileManager;
import dev.sadghost.espresso.spigot.files.IConfig;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bson.Document;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
@Singleton
public class MongoManager {
    private final MongoClient client;


    @Inject
    public MongoManager(final @NotNull FileManager fileManager,
                        final @NotNull Plugin plugin) {

        IConfig config = Preconditions.checkNonNull(fileManager.getConfig("config.yml"), "config is null");
        String connectionString = config.getString("mongodb.connection-string");
        if (connectionString == null) {
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            throw new IllegalStateException("Please set the database in the config.");
        }

        this.client = MongoClients.create(
                Objects.requireNonNull(config.getString("mongodb.connection-string"), "Connection string is not set up properly")
        );
    }

    public MongoCollection<Document> getPlayerCollection() {
        return client.getDatabase("skymines").getCollection("players");
    }

    public MongoClient getClient() {
        return client;
    }
}
