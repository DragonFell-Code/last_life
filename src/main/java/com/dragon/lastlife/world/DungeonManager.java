package com.dragon.lastlife.world;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.utils.Utils;
import com.google.common.collect.Lists;
import com.quiptmc.core.data.registries.Registries;
import com.quiptmc.core.data.registries.Registry;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.visitors.CollectToTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.bukkit.*;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.dragon.lastlife.world.Dungeon.DUNGEON_RESOURCE_KEY;
import static net.kyori.adventure.text.Component.text;
import static net.minecraft.world.entity.player.Inventory.*;
import static org.bukkit.persistence.PersistentDataType.STRING;

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
        Location tp_location = new Location(dungeon_world, -96, 100, -32);
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
