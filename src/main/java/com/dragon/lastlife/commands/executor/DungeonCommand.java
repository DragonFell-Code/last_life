package com.dragon.lastlife.commands.executor;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.commands.CommandExecutor;
import com.dragon.lastlife.config.Configs;
import com.dragon.lastlife.utils.Utils;
import com.dragon.lastlife.world.DungeonManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.PaperCommandSourceStack;
import io.papermc.paper.world.PaperWorldLoader;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Main;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.validation.ContentValidationException;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;

import java.io.IOException;
import java.lang.reflect.Field;

import static com.dragon.lastlife.world.DungeonManager.MAX_DUNGEON_SIZE;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class DungeonCommand extends CommandExecutor {
    public DungeonCommand(Initializer initializer) {
        super(initializer, "dungeon");
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> arguments() {
        Configs configs = Utils.configs();
        DungeonManager dungeonManager = configs.DUNGEON_MANAGER;
        return literal(name())
                .requires(source -> source.getSender().hasPermission("lastlife.admin"))
                .executes(context -> showUsage(context, ""))
                .then(literal("generate").executes(context -> {
                    generateDungeon(context, null);
                    return Command.SINGLE_SUCCESS;
                }).then(argument("size", IntegerArgumentType.integer(1, MAX_DUNGEON_SIZE)).executes(context -> {
                    int size = IntegerArgumentType.getInteger(context, "size");

                    generateDungeon(context, size);
                    return Command.SINGLE_SUCCESS;
                })))
                // TODO: Auto close after 1h ?
                .then(literal("close").executes(context -> {
                    return logError(context, "Not Implemented"); // TODO
                }))
                // TODO: Public announcement ?
                .then(literal("open").executes(context -> {
                    return logError(context, "Not Implemented"); // TODO
                }))
                .then(literal("reset").executes(context -> {
                    Location exit = dungeonManager.getDungeonExitLocation();
                    World dungeonWorld = dungeonManager.dungeon_world;

                    CraftServer server = (CraftServer) Bukkit.getServer();
                    CraftWorld world = (CraftWorld) dungeonWorld;

                    // Ensure we remove any player from the dimension
                    dungeonWorld.getPlayers().forEach(player -> player.teleport(exit));
                    // If dimension is still loaded, unload it
                    if (server.getServer().getLevel(world.getHandle().dimension()) != null && !Bukkit.getServer().unloadWorld(dungeonWorld, false)) {
                        return logError(context, "Failed to unload the Dungeon");
                    }
                    try {
                        FileUtils.deleteDirectory(dungeonWorld.getWorldFolder());
                    } catch (IOException e) {
                        return logError(context, "Failed to delete the Dungeon");
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
                        return logError(context, "Failed to getLevelData");
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
                        return logError(context, "Failed to load the level");
                    }
                    mc_server.prepareLevel(level);

                    dungeonManager.dungeon_world = Bukkit.getWorld(dungeonWorld.getName());

                    generateDungeon(context, null);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(literal("tp").executes(context -> {
                    Location entrance = dungeonManager.getDungeonEntranceLocation();
                    PaperCommandSourceStack source = (PaperCommandSourceStack) context.getSource();
                    ServerPlayer player = source.getHandle().getPlayerOrException();

                    if (entrance == null) {
                        return logError(context, "Dungeon entrance not found - is the dungeon open ?");
                    }

                    player.getBukkitEntity().teleport(entrance);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(literal("leave").executes(context -> {
                    Location exit = dungeonManager.getDungeonExitLocation();
                    PaperCommandSourceStack source = (PaperCommandSourceStack) context.getSource();
                    ServerPlayer player = source.getHandle().getPlayerOrException();

                    player.getBukkitEntity().teleport(exit);
                    return Command.SINGLE_SUCCESS;
                }));
    }

    private void generateDungeon(CommandContext<CommandSourceStack> context, Integer size) {
        Configs configs = Utils.configs();
        DungeonManager dungeonManager = configs.DUNGEON_MANAGER;
        CommandSender sender = context.getSource().getSender();

        sender.sendMessage(configs.MESSAGE_CONFIG.get("lastlife.cmd.dungeon.generating"));

        dungeonManager.create(dungeon -> {
            if (dungeon != null) {
                sender.sendMessage(configs.MESSAGE_CONFIG.get("lastlife.cmd.dungeon.open"));
            } else {
                sender.sendMessage(configs.MESSAGE_CONFIG.get("lastlife.cmd.dungeon.open_fail"));
            }
        });
    }
}
