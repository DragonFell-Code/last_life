package com.dragon.lastlife.world;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public record Dungeon(World world, DungeonManager manager) {

    public void generate(String structureName, int x, int y, int z) {
        spawnRandomStructure(structureName, x, y, z);
    }

    private StructureTemplate loadTemplate(World world, NamespacedKey key, String resourcePath) throws IOException {
        CraftWorld cw = (CraftWorld) world;
        StructureTemplateManager manager = cw.getHandle().getStructureManager();

        // Build the ResourceLocation that maps to your plugin namespace
        ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getKey());

        // Attempt to fetch an already-loaded template
        Optional<StructureTemplate> templateOptional = manager.get(rl);
        if (templateOptional.isPresent()) return templateOptional.get();


        // Not loaded yet? Read from your JAR’s resources
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream(resourcePath)) {
            if (in == null) throw new FileNotFoundException(resourcePath);
//            CompoundTag nbt = CompressedStreamTools.readCompressed(in);
            StructureTemplate template = manager.readStructure(in);
//            template.load(cw.getHandle(), nbt);
//            addTemplate(rl, template);
            return template;
        }
    }

    private void spawnRandomStructure(String structureName, int x, int y, int z) {
        try {
//            NamespacedKey key = new NamespacedKey(manager.initializer, structureName);
//             Load (or get cached) template
//            StructureTemplate tpl = loadTemplate(world, key, "structures/" + structureName + ".nbt");

            NamespacedKey key = NamespacedKey.minecraft("lastlife");
            StructureTemplate template = loadTemplate(world, key, "dungeon/entrances/entrance_1.nbt");

            // Choose a random rotation + mirror
            Mirror mirror = Mirror.values()[ThreadLocalRandom.current().nextInt(Mirror.values().length)];
            Rotation rot = Rotation.values()[ThreadLocalRandom.current().nextInt(Rotation.values().length)];

            // Build place settings
            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setMirror(mirror)
                    .setRotation(rot)
                    .setIgnoreEntities(false);

            // Decide spawn location (e.g., center of spawn)
            BlockPos pos = new BlockPos(x, y, z);

            // Perform placement
            CraftWorld cw = (CraftWorld) world;
            boolean placed = template.placeInWorld(
                    cw.getHandle(),   // Server World
                    BlockPos.ZERO,
                    pos,
                    settings,
                    RandomSource.create(),
                    2                // flags: 2 = notify neighbors
            );

            if (!placed) {
                manager.initializer.getLogger().warning("Structure placement failed!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
