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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Marker;

import java.time.Duration;
import java.util.Optional;

import static com.dragon.lastlife.world.DungeonManager.*;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class DungeonCommand extends CommandExecutor {
    public DungeonCommand(Initializer initializer) {
        super(initializer, "labyrinth");
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
                  }))
                )
                .then(literal("close").executes(context -> {
                    Optional<Entity> spawn_marker = dungeonManager.getMarker(PLAYER_SPAWN_MARKER_NAME);
                    CommandSender sender = context.getSource().getSender();

                    spawn_marker.ifPresent(Entity::remove);
                    dungeonManager.evictPlayers();

                    Bukkit.getServer().broadcast(configs.MESSAGE_CONFIG.get("lastlife.cmd.dungeon.closed"));
                    return Command.SINGLE_SUCCESS;
                }))
                .then(literal("open").executes(context -> {
                    Location spawn_location = dungeonManager.getMarkerLocation(SPAWN_MARKER_NAME);

                    if (spawn_location == null) {
                        return logError(context, "Failed to locate labyrinth spawn point - is the labyrinth generated ?");
                    }
                    if (dungeonManager.generating) {
                        return logError(context, "The labyrinth has not finished generating");
                    }

                    // Add a custom marker to TP in the dimension
                    dungeonManager.dungeon_world.spawn(spawn_location, Marker.class, marker -> {
                        marker.customName(Component.text(PLAYER_SPAWN_MARKER_NAME));
                    });
                    dungeonManager.handleNewDonationTotal(false); // Reset donation total to the actual value on dungeon opening

                    Title.Times times = Title.Times.times(Ticks.duration(10), Duration.ofSeconds(5), Ticks.duration(20));
                    Component message = configs.MESSAGE_CONFIG.get("lastlife.cmd.dungeon.open");
                    Title title = Title.title(message, Component.text("Head towards the portal at Spawn to enter the Labyrinth!"), times);
                    Bukkit.getServer().showTitle(title);
                    Bukkit.getServer().broadcast(message);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(literal("reset").executes(context -> resetDungeon(context, null))
                  .then(argument("size", IntegerArgumentType.integer(1, MAX_DUNGEON_SIZE)).executes(context -> {
                      int size = IntegerArgumentType.getInteger(context, "size");

                      resetDungeon(context, size);
                      return Command.SINGLE_SUCCESS;
                  }))
                )
                .then(literal("tp").executes(context -> {
                    Location entrance = dungeonManager.getDungeonEntranceLocation(SPAWN_MARKER_NAME);
                    PaperCommandSourceStack source = (PaperCommandSourceStack) context.getSource();
                    ServerPlayer player = source.getHandle().getPlayerOrException();

                    if (entrance == null) {
                        return logError(context, "Labyrinth entrance not found - is the labyrinth open ?");
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

        if (size == null) {
            size = dungeonManager.currentDungeonSize();
            sender.sendMessage(configs.MESSAGE_CONFIG.get("lastlife.cmd.dungeon.auto_size", size));
        }

        dungeonManager.create((dungeon, error) -> {
            if (dungeon != null) {
                sender.sendMessage(configs.MESSAGE_CONFIG.get("lastlife.cmd.dungeon.generated"));
            } else {
                sender.sendMessage(configs.MESSAGE_CONFIG.get("lastlife.cmd.dungeon.generate_fail", error));
            }
        }, size);
    }

    private int resetDungeon(CommandContext<CommandSourceStack> context, Integer size) {
        DungeonManager dungeonManager = Utils.configs().DUNGEON_MANAGER;

        if (dungeonManager.generating) {
            return logError(context, "Labyrinth is currently generating and cannot be interrupted");
        }
        try {
            dungeonManager.resetDimension();
            generateDungeon(context, size);
        } catch (Exception e) {
            return logError(context, e.getMessage());
        }
        return Command.SINGLE_SUCCESS;
    }
}
