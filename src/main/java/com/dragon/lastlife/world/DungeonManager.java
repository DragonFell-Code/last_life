package com.dragon.lastlife.world;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.utils.Utils;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.world.level.ChunkPos;
import org.bukkit.*;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

import static net.kyori.adventure.text.Component.text;

public class DungeonManager {
    public static int MAX_DUNGEON_SIZE = 30;
    public static String PLAYER_SPAWN_MARKER_NAME = "lastlife:dungeon/player_spawn";
    public static String SPAWN_MARKER_NAME = "lastlife:dungeon/spawn";
    // These coordinates are always the same, because of how the dungeon is generated in the custom dimension
    static Location dungeonSpawn = new Location(null, 0, 271, 0);

    public World dungeon_world;
    Initializer initializer;

    public DungeonManager(Initializer initializer) {
        this.initializer = initializer;
        String level_name = ((CraftServer)Bukkit.getServer()).getServer().getLevelIdName();
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
        World overworld = ((CraftServer)Bukkit.getServer()).getServer().overworld().getWorld();

        return overworld.getSpawnLocation().add(0.5, 0, 0.5);
    }

    public int currentDungeonLevel() {
        // TODO: Get lobby ID
        BigDecimal increments = BigDecimal.valueOf(false ? 1000 : 500);
        int level =  Utils.configs().DONATION_CONFIG().total.divide(increments, RoundingMode.DOWN).intValue();

        initializer.getLogger().info("Current Dungeon level: " + level + " (Size: " + dungeonLevelToSize(level) + ")");
        return level;
    }

    public int dungeonLevelToSize(int level) {
        return level * 2 + 6;
    }

    public void create(Consumer<Dungeon> callback, Integer size) {
        create(callback, size != null ? size : this.dungeonLevelToSize(this.currentDungeonLevel()));
    }

    public void create(Consumer<Dungeon> callback, int size) {
        if (this.getDungeonEntranceLocation() != null) {
            initializer.getComponentLogger().warn(text("Dungeon already exists!"), NamedTextColor.RED);
            callback.accept(null);
            return;
        }

        // TODO: Clear out dimension ?

        size = Math.clamp(size, 1, MAX_DUNGEON_SIZE);
        ChunkPos pos = new ChunkPos(dungeonSpawn.getBlockX() >> 4, dungeonSpawn.getBlockZ() >> 4);
        Dungeon dungeon = new Dungeon(dungeon_world, this, pos);

        dungeon_world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
        dungeon_world.setGameRule(GameRule.KEEP_INVENTORY, true);

        try {
            dungeon.generate(callback, size);
        } catch (Exception e) {
            initializer.getLogger().severe("Failed to generate dungeon structure: " + e.getMessage());
            callback.accept(null);
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
