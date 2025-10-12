package com.dragon.lastlife.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public record Dungeon(World world, DungeonManager manager) {
    static ResourceKey<Structure> DUNGEON_RESOURCE_KEY = ResourceKey.create(Registries.STRUCTURE, ResourceLocation.parse("lastlife:lastlife_dungeon"));

    public void generate(String structurePath, int x, int y, int z) {
        Holder.Reference<Structure> structure = MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(DUNGEON_RESOURCE_KEY);
        ServerLevel level = ((CraftWorld) world).getHandle();
        BlockPos pos = new BlockPos(x, y, z);

        Structure structure1 = structure.value();
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        StructureStart structureStart = structure1.generate(
                structure,
                level.dimension(),
                level.registryAccess(),
                generator,
                generator.getBiomeSource(),
                level.getChunkSource().randomState(),
                level.getStructureManager(),
                level.getSeed(),
                new ChunkPos(pos),
                0,
                level,
                biome -> true
        );

        if (!structureStart.isValid()) {
            manager.initializer.getLogger().severe("StructureStart is not Valid");
        } else {
            BoundingBox boundingBox = structureStart.getBoundingBox();
            ChunkPos start = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.minX()), SectionPos.blockToSectionCoord(boundingBox.minZ()));
            ChunkPos end = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.maxX()), SectionPos.blockToSectionCoord(boundingBox.maxZ()));

            ChunkPos.rangeClosed(start, end).filter(chunkPos -> !level.isLoaded(chunkPos.getWorldPosition())).forEach(chunkPos -> {
                world.getChunkAt(chunkPos.x, chunkPos.z); // force load
            });

            ChunkPos.rangeClosed(start, end)
                    .forEach(
                            chunkPos2 -> structureStart.placeInChunk(
                                    level,
                                    level.structureManager(),
                                    generator,
                                    level.getRandom(),
                                    new BoundingBox(
                                            chunkPos2.getMinBlockX(),
                                            level.getMinY(),
                                            chunkPos2.getMinBlockZ(),
                                            chunkPos2.getMaxBlockX(),
                                            level.getMaxY() + 1,
                                            chunkPos2.getMaxBlockZ()
                                    ),
                                    chunkPos2
                            )
                    );
            manager.initializer.getLogger().info("Structure has been Generated !");
        }

        // placeJigsaw(
        // (CommandSourceStack)context.getSource(),
        // ResourceKeyArgument.getStructureTemplatePool(context, "pool"),
        // ResourceLocationArgument.getId(context, "target"),
        // IntegerArgumentType.getInteger(context, "max_depth"),
        // new BlockPos(x, y, z))

        // JigsawPlacement.generateJigsaw(world, templatePool, target, 7, pos, false);

        // configuredFeature.place(level, level.getChunkSource().getGenerator(), level.getRandom(), pos);
        // String startPool = selectVillageStartPool();

        // boolean ok = generateJigsawFromPool(startPool, x, y, z, 7);
        // if (!ok) {
        // Fallback: place provided template if village generation fails
        // placeTemplate(structurePath, x, y, z);
        //}
    }

    private StructureTemplate loadTemplate(World world, NamespacedKey key, String resourcePath) throws IOException {
        CraftWorld cw = (CraftWorld) world;
        StructureTemplateManager stm = cw.getHandle().getStructureManager();

        ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getKey());

        Optional<StructureTemplate> templateOptional = stm.get(rl);
        if (templateOptional.isPresent()) return templateOptional.get();

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) throw new FileNotFoundException(resourcePath);
            return stm.readStructure(in);
        }
    }

    private void placeTemplate(String structurePath, int x, int y, int z) {
        try {
            // structurePath example: "dungeon/entrances/entrance_1_2"
            String namespacedPath = structurePath;
            if (namespacedPath.endsWith(".nbt")) {
                namespacedPath = namespacedPath.substring(0, namespacedPath.length() - 4);
            }

            NamespacedKey key = new NamespacedKey("lastlife", namespacedPath);
            String resourcePath = "lastlife/data/lastlife/structure/" + namespacedPath + ".nbt";
            StructureTemplate template = loadTemplate(world, key, resourcePath);

            // Choose a random rotation + mirror (or make deterministic if desired)
            Mirror mirror = Mirror.values()[ThreadLocalRandom.current().nextInt(Mirror.values().length)];
            Rotation rot = Rotation.values()[ThreadLocalRandom.current().nextInt(Rotation.values().length)];

            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setMirror(mirror)
                    .setRotation(rot)
                    .setIgnoreEntities(false);

            BlockPos pos = new BlockPos(x, y, z);

            CraftWorld cw = (CraftWorld) world;
            boolean placed = template.placeInWorld(
                    cw.getHandle(),
                    pos,
                    pos,
                    settings,
                    RandomSource.create(),
                    2
            );

            if (!placed) {
                manager.initializer.getLogger().warning("Structure placement failed at " + x + "," + y + "," + z + " for " + namespacedPath);
            } else {
                manager.initializer.getLogger().info("Placed structure " + namespacedPath + " at " + x + "," + y + "," + z);
            }
        } catch (IOException e) {
            manager.initializer.getLogger().severe("Error loading structure: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean generateJigsawFromPool(String startPoolId, int x, int y, int z, int size) {
        try {
            // Programmatic jigsaw assembly using NMS JigsawPlacement (no vanilla commands involved)
            ServerLevel nmsLevel = ((CraftWorld) world).getHandle();

            for(Map.Entry<ResourceLocation, Optional<StructureTemplate>> entry : nmsLevel.getStructureManager().structureRepository.entrySet()){
                System.out.println("Looking for \"lastlife\": " + entry.getKey().getNamespace());
                if(!entry.getKey().getNamespace().equals("lastlife")) continue;
                String namespacedPath = entry.getKey().getNamespace() + ":" + entry.getKey().getPath();
                System.out.println("Should start with \"" + startPoolId + "\": " + namespacedPath);
                if(!namespacedPath.startsWith(startPoolId)) continue;


                System.out.println(entry.getKey().getNamespace() + ":" + entry.getKey().getPath() + " -> " + (entry.getValue().isPresent() ? "LOADED" : "MISSING"));
                if(entry.getValue().isPresent()){
                    StructureTemplate template = entry.getValue().get();
                    System.out.println("  Size: " + template.getSize());
                    BlockPos pos = new BlockPos(x, y, z);
                    Mirror mirror = Mirror.values()[ThreadLocalRandom.current().nextInt(Mirror.values().length)];
                    Rotation rot = Rotation.values()[ThreadLocalRandom.current().nextInt(Rotation.values().length)];

                    StructurePlaceSettings settings = new StructurePlaceSettings()
                            .setMirror(mirror)
                            .setRotation(rot)
                            .setIgnoreEntities(false);

                    template.placeInWorld(
                            nmsLevel,
                            pos,
                            pos,
                            settings,
                            RandomSource.create(),
                            2);
                }

                return true;

            }
            if(true) return true;
            RegistryAccess access = nmsLevel.registryAccess();

            // Resolve start pool holder reflectively to avoid tight coupling to Mojang mappings between minor versions
            ResourceLocation rl = ResourceLocation.parse(startPoolId);
            ResourceKey<StructureTemplatePool> poolKey = ResourceKey.create(Registries.TEMPLATE_POOL, rl);

            Object holder; // Holder<StructureTemplatePool>
            try {
                // Preferred path: HolderGetter<StructureTemplatePool> lookup = access.lookupOrThrow(Registries.TEMPLATE_POOL);
                java.lang.reflect.Method lookupOrThrow = access.getClass().getMethod("lookupOrThrow", net.minecraft.core.Registry.class);
                Object holderGetter = lookupOrThrow.invoke(access, Registries.TEMPLATE_POOL);
                java.lang.reflect.Method getOrThrow = holderGetter.getClass().getMethod("getOrThrow", ResourceKey.class);
                holder = getOrThrow.invoke(holderGetter, poolKey);
            } catch (NoSuchMethodException nsme) {
                // Fallback path: Registry<StructureTemplatePool> reg = access.registryOrThrow(Registries.TEMPLATE_POOL); reg.getHolder(key)
                java.lang.reflect.Method registryOrThrow = access.getClass().getMethod("registryOrThrow", net.minecraft.core.Registry.class);
                Object registry = registryOrThrow.invoke(access, Registries.TEMPLATE_POOL);
                // Optional<Holder.Reference<...>> getHolder(ResourceKey)
                java.lang.reflect.Method getHolder = registry.getClass().getMethod("getHolder", ResourceKey.class);
                Object opt = getHolder.invoke(registry, poolKey);
                java.util.Optional<?> optional = (java.util.Optional<?>) opt;
                if (optional.isEmpty()) {
                    manager.initializer.getLogger().warning("Jigsaw start pool not found: " + startPoolId);
                    return false;
                }
                holder = optional.get();
            }

            BlockPos origin = new BlockPos(x, y, z);
            int seed = ThreadLocalRandom.current().nextInt();

            // Reflectively invoke JigsawPlacement.addPieces with whatever signature is available
            java.lang.reflect.Method target = null;
            for (java.lang.reflect.Method m : JigsawPlacement.class.getDeclaredMethods()) {
                if (!m.getName().equals("addPieces")) continue;
                Class<?>[] p = m.getParameterTypes();
                // We expect the first param to be ServerLevel and second to be some Holder type
                if (p.length >= 4 && ServerLevel.class.isAssignableFrom(p[0]) && p[1].getName().contains("Holder")) {
                    target = m;
                    break;
                }
            }
            if (target == null) {
                manager.initializer.getLogger().warning("Could not find JigsawPlacement.addPieces method to invoke.");
                return false;
            }
            target.setAccessible(true);

            Object[] args;
            int paramCount = target.getParameterCount();
            // Try to satisfy common signatures by arity
            if (paramCount == 8) {
                // (ServerLevel, Holder<Pool>, Optional<ResourceLocation>, int depth, BlockPos, boolean keepJigsaws, Optional<Heightmap.Types>, int seed)
                args = new Object[]{nmsLevel, holder, java.util.Optional.empty(), size, origin, Boolean.FALSE, java.util.Optional.empty(), seed};
            } else if (paramCount == 9) {
                // Sometimes includes an extra "maxDistanceFromCenter" before seed
                args = new Object[]{nmsLevel, holder, java.util.Optional.empty(), size, origin, Boolean.FALSE, java.util.Optional.empty(), Integer.valueOf(128), seed};
            } else if (paramCount == 10) {
                // Provide both maxDistance and a boolean for something like "respectProjection"
                args = new Object[]{nmsLevel, holder, java.util.Optional.empty(), size, origin, Boolean.FALSE, java.util.Optional.empty(), Integer.valueOf(128), Boolean.FALSE, seed};
            } else {
                // Best effort: build an array filling trailing params with sensible defaults
                args = new Object[paramCount];
                Class<?>[] p = target.getParameterTypes();
                for (int i = 0; i < paramCount; i++) {
                    if (i == 0) args[i] = nmsLevel;
                    else if (i == 1) args[i] = holder;
                    else if (p[i] == ResourceLocation.class) args[i] = null; // allow null if Optional not used
                    else if (p[i] == java.util.Optional.class) args[i] = java.util.Optional.empty();
                    else if (p[i] == int.class || p[i] == Integer.class) args[i] = (i < 7 ? size : 128);
                    else if (p[i] == boolean.class || p[i] == Boolean.class) args[i] = Boolean.FALSE;
                    else if (p[i] == BlockPos.class) args[i] = origin;
                    else args[i] = null;
                }
            }

            target.invoke(null, args);
            manager.initializer.getLogger().info("Generated jigsaw (programmatic) from pool " + startPoolId + " at " + x + "," + y + "," + z + " size=" + size);
            return true;
        } catch (Throwable t) {
            manager.initializer.getLogger().warning("Error generating jigsaw programmatically: " + t.getMessage());
            return false;
        }
    }

    private String selectVillageStartPool() {
        // Standard vanilla village start pools
        String[] pools = new String[] {
                "lastlife:dungeon/start"
//                "minecraft:village/plains/town_centers",
//                "minecraft:village/desert/town_centers",
//                "minecraft:village/savanna/town_centers",
//                "minecraft:village/taiga/town_centers",
//                "minecraft:village/snowy/town_centers"
        };
        int idx = ThreadLocalRandom.current().nextInt(pools.length);
        return pools[idx];
    }

}
