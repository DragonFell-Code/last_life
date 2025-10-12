package com.dragon.lastlife.world;

import com.dragon.lastlife.Initializer;
import com.quiptmc.core.data.registries.Registries;
import com.quiptmc.core.data.registries.Registry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import static net.kyori.adventure.text.Component.text;

public class DungeonManager {

    Initializer initializer;
    Registry<Dungeon> registry = Registries.register("lastlife:dungeons", () -> null);

    public DungeonManager(Initializer initializer) {
        this.initializer = initializer;
    }

    public Dungeon create(String name) {


        initializer.getComponentLogger().info(text("Creating dungeon world: " + name));
        if(registry.get(name).isPresent()){
            initializer.getComponentLogger().warn(text("Dungeon with name " + name + " already exists!"), NamedTextColor.RED);
            return registry.get(name).get();
        }
        World world;
        if(Bukkit.getWorld(name) != null){
            initializer.getComponentLogger().warn(text("World with name " + name + " already exists!"), NamedTextColor.RED);
            world = Bukkit.getWorld(name);
        } else {
            world = Bukkit.createWorld(new WorldCreator(name).generator(new VoidChunkGenerator()));
        }
        Dungeon dungeon = new Dungeon(world, this);
        registry.register(name, dungeon);
        // Ensure spawn chunk is loaded and place the dungeon start at 0,64,0
        world.getChunkAt(0, 0); // force load
        Bukkit.getScheduler().runTask(initializer, () -> {
            try {
                dungeon.generate("dungeon/entrances/entrance_1_2", 0, 64, 0);
            } catch (Exception e) {
                initializer.getLogger().warning("Failed to generate dungeon structure: " + e.getMessage());
            }
        });
        initializer.getComponentLogger().info("Done! Dungeon world created: " + name, NamedTextColor.GREEN);
        return dungeon;
    }


}
