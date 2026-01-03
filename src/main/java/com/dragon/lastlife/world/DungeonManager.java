package com.dragon.lastlife.world;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.loot.LootManager;
import com.dragon.lastlife.players.InventorySnapshot;
import com.dragon.lastlife.utils.Utils;
import io.papermc.paper.world.PaperWorldLoader;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Main;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.validation.ContentValidationException;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;

import static net.kyori.adventure.text.Component.text;

public class DungeonManager {
    public static double CHEST_REFRESH_DONATION_INCREMENT = 50.0;
    public static int MAX_DUNGEON_SIZE = 35;
    public static String PLAYER_SPAWN_MARKER_NAME = "lastlife:dungeon/player_spawn";
    public static String SPAWN_MARKER_NAME = "lastlife:dungeon/spawn";
    public static String GENERATION_COMPLETE_TAG = "generation_complete";
    public static final NamespacedKey KEY_DUNGEON_CHEST_LAST_DONATION_TOTAL = new NamespacedKey(Utils.initializer(), "last_donation_total");
    public static final NamespacedKey KEY_DUNGEON_CHEST_MARKER = new NamespacedKey(Utils.initializer(), "is_dungeon_chest");
    // These coordinates are always the same, because of how the dungeon is generated in the custom dimension
    static Location dungeonSpawn = new Location(null, 0, 271, 0);

    public World dungeon_world;
    Initializer initializer;
    public boolean generating = false;

    public DungeonManager(Initializer initializer) {
        this.initializer = initializer;
        String level_name = ((CraftServer) Bukkit.getServer()).getServer().getLevelIdName();
        dungeon_world = Bukkit.getWorld(level_name + "_lastlife_dungeon_dim");

        if (dungeon_world == null) {
            initializer.getLogger().severe("FAILED TO LOAD DUNGEON DIMENSION");
        }
    }

    public Location getDungeonEntranceLocation() {
        return this.getDungeonEntranceLocation(PLAYER_SPAWN_MARKER_NAME);
    }

    public Location getDungeonEntranceLocation(String name) {
        return this.getMarkerLocation(name);
    }

    public Location getDungeonExitLocation() {
        World overworld = ((CraftServer) Bukkit.getServer()).getServer().overworld().getWorld();

        return overworld.getSpawnLocation().add(0.5, 0, 0.5);
    }

    public void evictPlayers() {
        Location exit = this.getDungeonExitLocation();
        World dungeonWorld = this.dungeon_world;

        dungeonWorld.getPlayers().forEach(player -> {
            player.teleport(exit);
            InventorySnapshot.forceApplyInventorySnapshot(((CraftPlayer) player).getHandle());
        });
    }

    public int currentDungeonSize() {
        return dungeonLevelToSize(LootManager.getDonationLevel());
    }

    public int dungeonLevelToSize(int level) {
        return level * 2 + 6;
    }

    public void create(BiConsumer<Dungeon, String> callback, Integer size) {
        create(callback, size != null ? size : this.currentDungeonSize());
    }

    public void create(BiConsumer<Dungeon, String> callback, int size) {
        Entity marker = this.getMarker(SPAWN_MARKER_NAME).orElse(null);

        if (generating) {
            String error = "Labyrinth generation is already in progress";
            initializer.getComponentLogger().warn(text(error), NamedTextColor.RED);
            callback.accept(null, error);
            return;
        }

        if (marker != null) {
            if (marker.getScoreboardTags().contains(GENERATION_COMPLETE_TAG)) {
                String error = "Labyrinth already generated!";
                initializer.getComponentLogger().warn(text(error), NamedTextColor.RED);
                callback.accept(null, error);
                return;
            }

            resetDimension();
        }

        size = Math.clamp(size, 1, MAX_DUNGEON_SIZE);
        ChunkPos pos = new ChunkPos(dungeonSpawn.getBlockX() >> 4, dungeonSpawn.getBlockZ() >> 4);
        Dungeon dungeon = new Dungeon(dungeon_world, this, pos);
        BiConsumer<Dungeon, String> parentCallback = (newDungeon, error) -> {
            generating = false;
            handleNewDonationTotal(); // Save initial donation total
            callback.accept(newDungeon, error);
        };

        dungeon_world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
        dungeon_world.setGameRule(GameRule.KEEP_INVENTORY, true);

        try {
            generating = true;
            dungeon.generate(parentCallback, size);
        } catch (Exception e) {
            String error = "Failed to generate labyrinth structure";
            initializer.getLogger().severe(error + ": " + e.getMessage());
            callback.accept(null, error);
        }
    }

    public void resetDimension() {
        CraftServer server = (CraftServer) Bukkit.getServer();
        CraftWorld world = (CraftWorld) dungeon_world;

        // Ensure we remove any player from the dimension
        this.evictPlayers();
        // If dimension is still loaded, unload it
        if (server.getServer().getLevel(world.getHandle().dimension()) != null && !Bukkit.getServer().unloadWorld(dungeon_world, false)) {
            throw new RuntimeException("Failed to unload the Labyrinth");
        }
        try {
            FileUtils.deleteDirectory(dungeon_world.getWorldFolder());
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete the Labyrinth");
        }

        DedicatedServer mc_server = server.getServer();
        PaperWorldLoader loader = PaperWorldLoader.create(mc_server, mc_server.storageSource.getLevelId());

        ResourceLocation dungeon_resource_location = ResourceLocation.fromNamespaceAndPath("lastlife", "dungeon_dim");
        ResourceKey<Level> dungeon_level_key = ResourceKey.create(Registries.DIMENSION, dungeon_resource_location);

        LevelStem stem = mc_server.registryAccess().lookupOrThrow(Registries.LEVEL_STEM).get(dungeon_resource_location).orElseThrow().value();
        ResourceKey<LevelStem> stemKey = mc_server.registryAccess().lookupOrThrow(Registries.LEVEL_STEM).getResourceKey(stem).orElseThrow();
        String worldType = stemKey.location().getNamespace() + "_" + stemKey.location().getPath();
        PaperWorldLoader.WorldLoadingInfo info = new PaperWorldLoader.WorldLoadingInfo(-999, loader.levelId() + "_" + worldType, worldType, stemKey, true);

        LevelStorageSource.LevelStorageAccess levelStorageAccess;
        try {
            levelStorageAccess = LevelStorageSource.createDefault(mc_server.server.getWorldContainer().toPath()).validateAndCreateAccess(info.name(), info.stemKey());
        } catch (IOException | ContentValidationException e) {
            throw new RuntimeException(e);
        }
        PaperWorldLoader.LevelDataResult levelData = PaperWorldLoader.getLevelData(levelStorageAccess);
        if (levelData.fatalError()) {
            throw new RuntimeException("Failed to getLevelData");
        }

        final PrimaryLevelData primaryLevelData;
        DedicatedServerSettings settings = mc_server.settings;

        if (levelData.dataTag() == null) {
            primaryLevelData = (PrimaryLevelData) Main.createNewWorldData(
                    settings,
                    mc_server.worldLoaderContext,
                    mc_server.worldLoaderContext.datapackDimensions().lookupOrThrow(Registries.LEVEL_STEM),
                    mc_server.isDemo(),
                    mc_server.options.has("bonusChest")
            ).cookie();
        } else {
            primaryLevelData = (PrimaryLevelData) LevelStorageSource.getLevelDataAndDimensions(
                    levelData.dataTag(),
                    mc_server.worldLoaderContext.dataConfiguration(),
                    mc_server.worldLoaderContext.datapackDimensions().lookupOrThrow(Registries.LEVEL_STEM),
                    mc_server.worldLoaderContext.datapackWorldgen()
            ).worldData();
        }

        // MC doesn't expose a method to change the seed, relying on Reflection. Otherwise, a dungeon ""Reset"" would generate the same dungeon twice
        try {
            Field worldOptionsField = PrimaryLevelData.class.getDeclaredField("worldOptions");

            worldOptionsField.setAccessible(true);
            worldOptionsField.set(primaryLevelData, primaryLevelData.worldGenOptions().withSeed(java.util.OptionalLong.empty()));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        primaryLevelData.checkName(info.name()); // CraftBukkit - Migration did not rewrite the level.dat; This forces 1.8 to take the last loaded world as respawn (in this case the end)
        primaryLevelData.setModdedInfo(mc_server.getServerModName(), mc_server.getModdedStatus().shouldReportAsModified());

        mc_server.createLevel(stem, info, levelStorageAccess, primaryLevelData);
        ServerLevel level = mc_server.getLevel(dungeon_level_key);

        if (level == null) {
            throw new RuntimeException("Failed to load the level");
        }
        mc_server.prepareLevel(level);

        dungeon_world = Bukkit.getWorld(dungeon_world.getName());
    }

    public void registerDonationTotalOnMarker(double total) {
        getMarker(SPAWN_MARKER_NAME).ifPresent(marker -> {
            marker.getPersistentDataContainer().set(KEY_DUNGEON_CHEST_LAST_DONATION_TOTAL, PersistentDataType.DOUBLE, total);
        });
    }

    public double getLastDonationTotalFromMarker() {
        return getMarker(SPAWN_MARKER_NAME).map(marker -> {
            return marker.getPersistentDataContainer().getOrDefault(KEY_DUNGEON_CHEST_LAST_DONATION_TOTAL, PersistentDataType.DOUBLE, 0.0);
        }).orElse(0.0);
    }

    public void handleNewDonationTotal() {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(initializer, this::handleNewDonationTotal);
            return;
        }
        double total = Utils.configs().DONATION_CONFIG().total.doubleValue();
        double currentTotal = getLastDonationTotalFromMarker();

        if (total >= currentTotal + CHEST_REFRESH_DONATION_INCREMENT) {
            // Make sure we don't eat the reminder. If user donates 99$ we should refresh once,
            // but only increase our total by 50$, so we only need another 1$ to get to the next refresh
            double newTotal = total - (total % CHEST_REFRESH_DONATION_INCREMENT);

            registerDonationTotalOnMarker(newTotal);
        }
    }

    public Optional<Entity> getMarker(String marker_name) {
        if (dungeon_world == null) {
            return Optional.empty();
        }
        Location tp_location = dungeonSpawn.toLocation(dungeon_world);
        Chunk chunk = tp_location.getChunk();
        ChunkPos chunkPos = new ChunkPos(chunk.getX(), chunk.getZ());

        if (!((CraftWorld) dungeon_world).getHandle().isLoaded(chunkPos.getWorldPosition())) {
            dungeon_world.getChunkAt(chunkPos.x, chunkPos.z); // force load
        }

        Collection<Entity> entities = dungeon_world.getNearbyEntities(tp_location, 5, 5, 5);

        return entities.stream().filter(entity -> marker_name.equals(entity.getName())).findFirst();
    }

    public Location getMarkerLocation(String marker_name) {
        return getMarker(marker_name).map(Entity::getLocation).orElse(null);
    }
}
