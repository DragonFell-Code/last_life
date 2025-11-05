package com.dragon.lastlife;

import com.dragon.lastlife.commands.CommandExecutor;
import com.dragon.lastlife.commands.executor.*;
import com.dragon.lastlife.listeners.EntityTeleportEventListener;
import com.dragon.lastlife.listeners.FoxPersistenceListener;
import com.dragon.lastlife.listeners.LootListener;
import com.dragon.lastlife.listeners.PlayerListener;
import com.dragon.lastlife.utils.Utils;
import com.quiptmc.core.QuiptIntegration;
import com.quiptmc.core.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Fox;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public final class Initializer extends JavaPlugin {
    private LastLife integration;

    public PlayerListener PLAYER_LISTENER = new PlayerListener();
    public LootListener LOOT_LISTENER = new LootListener();
    public FoxPersistenceListener FOX_PERSISTENCE_LISTENER = new FoxPersistenceListener();
    public EntityTeleportEventListener ENTITY_TELEPORT_LISTENER = new EntityTeleportEventListener();

    private final List<Listener> listeners = Arrays.asList(
            PLAYER_LISTENER, LOOT_LISTENER, FOX_PERSISTENCE_LISTENER, ENTITY_TELEPORT_LISTENER
    );

    public void quipt() {
        integration = new LastLife();
        integration.enable();
    }

    @Override
    public void onEnable() {
        quipt();
        Utils.init(this);

        PluginManager pluginManager = this.getServer().getPluginManager();
        listeners.forEach(listener -> pluginManager.registerEvents(listener, this));

        Bukkit.getWorlds().forEach(world -> world.getEntitiesByClass(Fox.class).forEach(fox -> FOX_PERSISTENCE_LISTENER.handleFoxEntity(fox)));

        new CommandExecutor.Builder(new ConfigCommand(this)).setDescription("Manage Last Life configuration files").register();
        new CommandExecutor.Builder(new BoogeyCommand(this)).setDescription("Manage Last Life boogeys").register();
        new CommandExecutor.Builder(new DonationsCommand(this)).setDescription("Base command for participants to link their ExtraLife accounts").register();
        new CommandExecutor.Builder(new LifeCommand(this)).setDescription("Manage player lives").register();
        new CommandExecutor.Builder(new DungeonCommand(this)).register();
        new CommandExecutor.Builder(new PartyCommand(this)).register();
        getLogger().info("LastLife plugin has been enabled successfully.");
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

        public void warn(String tag, String message) {
            getLogger().warning("[%s] %s".formatted(tag, message));
        }
    }
}
