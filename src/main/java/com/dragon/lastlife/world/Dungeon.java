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

    public void generate(String structurePath, int x, int y, int z) {
        // Try to generate the full jigsaw structure if the id looks like a configured structure
        // Fallback to raw template placement if jigsaw fails.
        boolean jigsawOk = generateJigsaw("lastlife:lastlife_dungeon", x, y, z);
        if (!jigsawOk) {
            placeTemplate(structurePath, x, y, z);
        }
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

    private boolean generateJigsaw(String structureId, int x, int y, int z) {
        try {
            // Build a CommandSourceStack bound to this world at the target position and run /place structure
            net.minecraft.world.level.Level nmsLevel = ((CraftWorld) world).getHandle();
            net.minecraft.server.MinecraftServer server = ((net.minecraft.server.level.ServerLevel) nmsLevel).getServer();
            net.minecraft.commands.CommandSourceStack source = new net.minecraft.commands.CommandSourceStack(
                    net.minecraft.commands.CommandSource.NULL,
                    new net.minecraft.world.phys.Vec3(x + 0.5, y, z + 0.5),
                    net.minecraft.world.phys.Vec2.ZERO,
                    (net.minecraft.server.level.ServerLevel) nmsLevel,
                    4,
                    "LastLife",
                    net.minecraft.network.chat.Component.literal("LastLife"),
                    server,
                    null
            );
            String cmd = "place structure " + structureId + " " + x + " " + y + " " + z;
            server.getCommands().performPrefixedCommand(source, cmd);
            // If the above didn't throw, we assume success. If nothing placed, try jigsaw pool form.
            String jCmd = "place jigsaw lastlife:dungeon/start " + x + " " + y + " " + z + " 7";
            server.getCommands().performPrefixedCommand(source, jCmd);
            manager.initializer.getLogger().info("Requested jigsaw placement for (" + structureId + ") at " + x + "," + y + "," + z + ".");
            return true;
        } catch (Throwable t) {
            manager.initializer.getLogger().warning("Error running jigsaw placement: " + t.getMessage());
            return false;
        }
    }

}
