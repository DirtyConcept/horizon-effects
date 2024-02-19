package net.horizonmines.effects.factories;

import dev.sadghost.espresso.spigot.files.FileManager;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import org.bukkit.plugin.java.JavaPlugin;

@Factory
public final class ConfigFactory {

    @SuppressWarnings("UnstableApiUsage")
    @Bean
    public FileManager fileManager(JavaPlugin plugin) {
        return new FileManager(plugin);
    }
}
