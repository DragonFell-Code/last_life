package com.dragon.lastlife.world;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.utils.Utils;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.world.level.ChunkPos;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
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
    // These coordinates are always the same, because of how the dungeon is generated in the custom dimension
    static Location dungeonSpawn = new Location(null, 0, 271, 0);

    public World dungeon_world;
    Initializer initializer;

    public DungeonManager(Initializer initializer) {
        this.initializer = initializer;
        dungeon_world = Bukkit.getWorld("world_lastlife_dungeon_dim");
    }

    public Location getDungeonEntranceLocation() {
        if (dungeon_world == null) {
            return null;
        }
        Location tp_location = dungeonSpawn.toLocation(dungeon_world);
        Chunk chunk = tp_location.getChunk();
        ChunkPos chunkPos = new ChunkPos(chunk.getX(), chunk.getZ());

        if (!((CraftWorld) dungeon_world).getHandle().isLoaded(chunkPos.getWorldPosition())) {
            dungeon_world.getChunkAt(chunkPos.x, chunkPos.z); // force load
        }

        Collection<Entity> entities = dungeon_world.getNearbyEntities(tp_location, 5, 5, 5);
        Optional<Entity> spawn_marker = entities.stream().filter(entity -> "lastlife:dungeon/spawn".equals(entity.getName())).findFirst();

        return spawn_marker.map(Entity::getLocation).orElse(null);
    }

    public Location getDungeonExitLocation() {
        World overworld = Bukkit.getWorlds().getFirst(); // TODO: Does this always work ?

        // TODO: Do we want custom coordinates instead ?
        return overworld.getSpawnLocation();
    }

    public void create(Consumer<Dungeon> callback) {
        create(callback, currentDungeonLevel());
    }

    public int currentDungeonLevel() {
        // TODO: Get looby ID
        BigDecimal increments = BigDecimal.valueOf(false ? 1000 : 500);
        int level =  Utils.configs().DONATION_CONFIG().total.divide(increments, RoundingMode.DOWN).intValue();

        initializer.getLogger().info("Current Dungeon level: " + level);
        return level;
    }

    public void create(Consumer<Dungeon> callback, int size) {
        if (getDungeonEntranceLocation() != null) {
            initializer.getComponentLogger().warn(text("Dungeon already exists!"), NamedTextColor.RED);
            callback.accept(null);
            return;
        }

        size = Math.clamp(size, 1, MAX_DUNGEON_SIZE);

        ChunkPos pos = new ChunkPos(dungeonSpawn.getBlockX() >> 4, dungeonSpawn.getBlockZ() >> 4);
        Dungeon dungeon = new Dungeon(dungeon_world, this, pos);

        try {
            dungeon.generate(callback, size);
        } catch (Exception e) {
            initializer.getLogger().warning("Failed to generate dungeon structure: " + e.getMessage());
            callback.accept(null);
        }
    }
}
