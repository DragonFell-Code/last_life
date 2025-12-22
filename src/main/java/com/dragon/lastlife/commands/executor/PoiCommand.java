package com.dragon.lastlife.commands.executor;


import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.commands.CommandExecutor;
import com.dragon.lastlife.commands.executor.argument.PoiArgumentType;
import com.dragon.lastlife.config.object.ConfigLocation;
import com.dragon.lastlife.utils.Utils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.math.BlockPosition;
import org.bukkit.entity.Player;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class PoiCommand extends CommandExecutor {
    public PoiCommand(Initializer initializer) {
        super(initializer, "poi");
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> arguments() {
        return literal(name())
                .executes(context -> showUsage(context, ""))
                .then(literal("add")
                        .executes(context -> showUsage(context, "lastlife.poi.add"))
                        .then(argument("id", StringArgumentType.string())
                                .executes(context -> {
                                    if (!(context.getSource().getSender() instanceof Player player))
                                        return logError(context, "You must be a player to run this command.");
                                    if (!player.hasPermission("lastlife.poi.add"))
                                        return logError(context, "You do not have permission to use this command.");
                                    String id = StringArgumentType.getString(context, "id");
                                    ConfigLocation location = new ConfigLocation(id, player.getLocation().blockX(), player.getLocation().blockY(), player.getLocation().blockZ(), player.getLocation().getYaw(), player.getLocation().getPitch(), player.getLocation().getWorld().getName());
                                    Utils.configs().POI_CONFIG().pois.put(location);
                                    Utils.configs().POI_CONFIG().save();
                                    player.sendMessage(Utils.configs().MESSAGE_CONFIG.get("cmd.poi.add", id));
                                    return 1;
                                })
                                .then(argument("blockPos", ArgumentTypes.blockPosition())
                                        .executes(context -> {
                                            if (!(context.getSource().getSender() instanceof Player player))
                                                return logError(context, "You must be a player to run this command.");
                                            if (!player.hasPermission("lastlife.poi.add"))
                                                return logError(context, "You do not have permission to use this command.");
                                            String id = StringArgumentType.getString(context, "id");
                                            BlockPositionResolver blockPosResolver = context.getArgument("blockPos", BlockPositionResolver.class);
                                            BlockPosition blockPos = blockPosResolver.resolve(context.getSource());
                                            Utils.configs().POI_CONFIG().pois.put(new ConfigLocation(id, blockPos.x(), blockPos.y(), blockPos.z(), player.getLocation().getYaw(), player.getLocation().getPitch(), player.getLocation().getWorld().getName()));
                                            Utils.configs().POI_CONFIG().save();
                                            player.sendMessage(Utils.configs().MESSAGE_CONFIG.get("cmd.poi.add", id));
                                            return 1;
                                        }))))
                .then(literal("remove")
                        .executes(context -> showUsage(context, "lastlife.poi.remove"))
                        .then(argument("id", new PoiArgumentType())
                                .executes(context -> {
                                    if (!context.getSource().getSender().hasPermission("lastlife.poi.remove"))
                                        return logError(context, "You do not have permission to use this command.");
                                    ConfigLocation loc = context.getArgument("id", ConfigLocation.class);
                                    Utils.configs().POI_CONFIG().pois.remove(loc);
                                    Utils.configs().POI_CONFIG().save();
                                    context.getSource().getSender().sendMessage(Utils.configs().MESSAGE_CONFIG.get("cmd.poi.remove", loc.id()));
                                    return 1;
                                })));
    }
}
