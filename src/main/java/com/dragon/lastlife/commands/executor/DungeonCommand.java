package com.dragon.lastlife.commands.executor;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.commands.CommandExecutor;
import com.dragon.lastlife.config.Configs;
import com.dragon.lastlife.utils.Utils;
import com.dragon.lastlife.world.DungeonManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import static io.papermc.paper.command.brigadier.Commands.literal;

public class DungeonCommand extends CommandExecutor {
    public DungeonCommand(Initializer initializer) {
        super(initializer, "dungeon");
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> arguments() {
        return literal(name())
                .executes(context -> showUsage(context, ""))
                .then(literal("open")
                        .executes(context -> {
                            if (!context.getSource().getSender().hasPermission("lastlife.admin"))
                                return logError(context, "You do not have permission to use this command.");

                            Utils.configs().DUNGEON_MANAGER.create(dungeon -> {
                                if (dungeon != null) {
                                    context.getSource().getSender().sendMessage(Utils.configs().MESSAGE_CONFIG.get("lastlife.cmd.dungeon.open"));
                                } else {
                                    context.getSource().getSender().sendMessage(Utils.configs().MESSAGE_CONFIG.get("lastlife.cmd.dungeon.open_fail"));
                                }
                            });
                            return Command.SINGLE_SUCCESS;
                        }));
    }
}
