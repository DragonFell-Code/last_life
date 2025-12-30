package com.dragon.lastlife.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.dragon.lastlife.world.DungeonManager.SPAWN_MARKER_NAME;

public class Dungeon {
    static ResourceKey<Structure> DUNGEON_RESOURCE_KEY = ResourceKey.create(Registries.STRUCTURE, ResourceLocation.parse("lastlife:lastlife_dungeon"));

    Dungeon(World world, DungeonManager manager, ChunkPos chunkPos) {
        this.world = world;
        this.manager = manager;
        this.chunkPos = chunkPos;
        this.level = ((CraftWorld) world).getHandle();

        this.origin = null;
    }

    public World world;
    public ServerLevel level;
    public DungeonManager manager;
    public BlockPos origin;
    public ChunkPos chunkPos;

    public void generate(Consumer<Dungeon> callback, int size) {
        Holder.Reference<Structure> structureRef = MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(DUNGEON_RESOURCE_KEY);

        JigsawStructure structure = (JigsawStructure) structureRef.value();
        ChunkGenerator generator = level.getChunkSource().getGenerator();

        // MC doesn't expose a method to change the depth, relying on Reflection
        try {
            Field maxDepthField = JigsawStructure.class.getDeclaredField("maxDepth");
            Field maxDistanceFromCenterField = JigsawStructure.class.getDeclaredField("maxDistanceFromCenter");

            maxDistanceFromCenterField.setAccessible(true);
            maxDepthField.setAccessible(true);
            maxDepthField.set(structure, size);

            // Remove max horizontal distance
            maxDistanceFromCenterField.set(structure, new JigsawStructure.MaxDistance(1000, 1000));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        // Generation can take a while, running it in a separate thread to not block the server
        Bukkit.getScheduler().runTaskAsynchronously(manager.initializer, () -> {
            try {
                StructureStart structureStart = structure.generate(
                        structureRef,
                        level.dimension(),
                        level.registryAccess(),
                        generator,
                        generator.getBiomeSource(),
                        level.getChunkSource().randomState(),
                        level.getStructureManager(),
                        level.getSeed(),
                        chunkPos,
                        0,
                        level,
                        biome -> true
                );

                if (!structureStart.isValid()) {
                    manager.initializer.getLogger().severe("StructureStart is not Valid");
                    callback.accept(null);
                    return;
                }

                manager.initializer.getLogger().info("StructureStart has been Generated. Placing down the pieces...");

                origin = structureStart.getPieces().getFirst().getBoundingBox().getCenter();

                BoundingBox boundingBox = structureStart.getBoundingBox();
                ChunkPos start = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.minX()), SectionPos.blockToSectionCoord(boundingBox.minZ()));
                ChunkPos end = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.maxX()), SectionPos.blockToSectionCoord(boundingBox.maxZ()));

                List<ChunkPos> chunks = ChunkPos.rangeClosed(start, end).collect(Collectors.toCollection(LinkedList::new));

                manager.initializer.getLogger().info("Total chunks to generate : " + chunks.size());
                // Start generation
                this.generateNext(structureStart, chunks, callback);
            } catch (Exception e) {
                manager.initializer.getLogger().severe("Failed to generate dungeon: " + e.getMessage());
                callback.accept(null);
            }
        });
    }

    private void generateNext(StructureStart structure, List<ChunkPos> remainingChunks, Consumer<Dungeon> callback) {
        if (remainingChunks.isEmpty()) {
            if (manager.getDungeonEntranceLocation(SPAWN_MARKER_NAME) != null) {
                manager.initializer.getLogger().info("Structure has been Generated !");
                callback.accept(this);
            } else {
                manager.initializer.getLogger().severe("Failed to generate the dungeon spawn");
                callback.accept(null);
            }
            return;
        }

        // Delay generation to the next server Tick
        Bukkit.getScheduler().runTaskLater(manager.initializer, () -> {
            try {
                ChunkPos chunkPos = remainingChunks.removeFirst();
                if (!level.isLoaded(chunkPos.getWorldPosition())) {
                    world.getChunkAt(chunkPos.x, chunkPos.z); // force load
                }

                ChunkGenerator generator = level.getChunkSource().getGenerator();
                BoundingBox boundingBox = new BoundingBox(
                        chunkPos.getMinBlockX(), level.getMinY(), chunkPos.getMinBlockZ(),
                        chunkPos.getMaxBlockX(), level.getMaxY() + 1, chunkPos.getMaxBlockZ()
                );
                structure.placeInChunk(level, level.structureManager(), generator, level.getRandom(), boundingBox, chunkPos);

                // Generate next chunk of the structure
                this.generateNext(structure, remainingChunks, callback);
            } catch (Exception e) {
                manager.initializer.getLogger().severe("Failed to generate dungeon: " + e.getMessage());
                callback.accept(null);
            }
        }, 1);
    }
}
