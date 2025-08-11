package com.dragon.lastlife;

import com.dragon.lastlife.utils.Utils;
import com.quiptmc.core.QuiptIntegration;
import com.quiptmc.core.config.ConfigManager;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.java.JavaPlugin;

import com.dragon.lastlife.listeners.PlayerListener;

import java.awt.*;
import java.io.File;

public final class Initializer extends JavaPlugin {

    private LastLife integration;

    @Override
    public void onEnable() {
        integration = new LastLife();
        integration.enable();
        Utils.init(this);
        getLogger().info("LastLife plugin has been enabled successfully.");
        new PlayerListener(this);
    }

    public LastLife integration() {
        return integration;
    }

    @Override
    public void onDisable() {
        ConfigManager.saveAll();
        // Plugin shutdown logic
    }

    public class LastLife extends QuiptIntegration {

        File dataFolder = new File("plugins/" + getName());


        @Override
        public void enable() {
            if (!dataFolder.exists()) {
                log("Initializer", "Attempting to create data folder (" + dataFolder.getPath() + "): " + dataFolder.mkdirs());
            }
            // Initialize other components here, e.g., HeartbeatUtils, LocationUtils, etc.
        }

        @Override
        public void log(String tag, String message) {
            getLogger().info("[%s] %s".formatted(tag, message));
        }

        @Override
        public File dataFolder() {
            return dataFolder;
        }

        @Override
        public String name() {
            return getName();
        }

        @Override
        public String version() {
            return getPluginMeta().getVersion();
        }

        public void warn(String s, String id) {
            getLogger().warning("[%s] %s".formatted(s, id));
        }
    }
}
