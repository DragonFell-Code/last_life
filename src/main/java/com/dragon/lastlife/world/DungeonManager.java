package com.dragon.lastlife.world;

import com.dragon.lastlife.Initializer;
import com.quiptmc.core.data.registries.Registries;
import com.quiptmc.core.data.registries.Registry;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.world.level.ChunkPos;
import org.bukkit.*;
import org.bukkit.entity.Entity;

import java.util.*;
import java.util.function.Consumer;

import static com.dragon.lastlife.world.Dungeon.DUNGEON_RESOURCE_KEY;
import static net.kyori.adventure.text.Component.text;

public class DungeonManager {

    public World dungeon_world;

    Initializer initializer;
    Registry<Dungeon> registry = Registries.register("lastlife:dungeons", () -> null);

    public DungeonManager(Initializer initializer) {
        this.initializer = initializer;
        dungeon_world = Bukkit.getWorld("world_lastlife_dungeon_dim");
    }

    public Location getDungeonEntranceLocation() {
        if (dungeon_world == null) {
            return null;
        }
        // These coordinates are always the same, because of how the dungeon is generated in the custom dimension
        Location tp_location = new Location(dungeon_world, -96, 272, -32);
        Collection<Entity> entities = dungeon_world.getNearbyEntities(tp_location, 7, 1, 7);
        Optional<Entity> spawn_marker = entities.stream().filter(entity -> "lastlife:dungeon/spawn".equals(entity.getName())).findFirst();

        if (spawn_marker.isPresent()) {
            tp_location = spawn_marker.get().getLocation();
        }
        return tp_location;
    }

    public Location getDungeonExitLocation() {
        World overworld = Bukkit.getWorlds().getFirst(); // TODO: Does this always work ?

        // TODO: Do we want custom coordinates instead ?
        return overworld.getSpawnLocation();
    }

    public void create(String name, ChunkPos pos, Consumer<Dungeon> callback) {
        initializer.getComponentLogger().info(text("Creating dungeon world: " + name));
        if (registry.get(name).isPresent()) {
            initializer.getComponentLogger().warn(text("Dungeon with name " + name + " already exists!"), NamedTextColor.RED);
            callback.accept(registry.get(name).get());
            return;
        }

        Bukkit.getScheduler().runTask(initializer, () -> {
            World world = Bukkit.getWorld(name);

            if (world != null) {
                initializer.getComponentLogger().warn(text("World with name " + name + " already exists!"), NamedTextColor.RED);
            } else {
                world = Bukkit.createWorld(new WorldCreator(name).generator(new VoidChunkGenerator()));
                initializer.getComponentLogger().info(text("Done! Dungeon world created: " + name), NamedTextColor.GREEN);
            }
            Dungeon dungeon = new Dungeon(world, this);

            try {
                dungeon.generate(DUNGEON_RESOURCE_KEY, pos);
                registry.register(name, dungeon);

                callback.accept(dungeon);
            } catch (Exception e) {
                initializer.getLogger().warning("Failed to generate dungeon structure: " + e.getMessage());
            }
        });
    }
}
