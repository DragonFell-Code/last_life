package com.dragon.lastlife.commands.executor;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.commands.CommandExecutor;
import com.dragon.lastlife.config.Configs;
import com.dragon.lastlife.listeners.LootListener;
import com.dragon.lastlife.utils.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import static com.dragon.lastlife.listeners.LootListener.MAX_DONATION_LEVEL;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class LootLevelCommand extends CommandExecutor {
    public LootLevelCommand(Initializer initializer) {
        super(initializer, "loot_level");
    }

    public LiteralArgumentBuilder<CommandSourceStack> arguments() {
        Configs configs = Utils.configs();
        return literal(name())
                .requires(source -> source.getSender().hasPermission("lastlife.admin"))
                .executes(context -> showUsage(context, ""))
                .then(literal("get").executes(context -> {
                    CommandSender sender = context.getSource().getSender();
                    int level = LootListener.getDonationLevel();
                    String overridden = LootListener.donation_level_override != null ? " (overridden)" : "";

                    sender.sendMessage(configs.MESSAGE_CONFIG.get("lastlife.cmd.loot_level.get", level, overridden));
                    return Command.SINGLE_SUCCESS;
                }))
                .then(literal("set").then(argument("level", IntegerArgumentType.integer(1, MAX_DONATION_LEVEL)).executes(context -> {
                    int level = IntegerArgumentType.getInteger(context, "level");
                    CommandSender sender = context.getSource().getSender();

                    LootListener.setDonationLevelOverride(level);
                    sender.sendMessage(configs.MESSAGE_CONFIG.get("lastlife.cmd.loot_level.set", LootListener.getDonationLevel()));
                    return Command.SINGLE_SUCCESS;
                })))
                .then(literal("reset").executes(context -> {
                    CommandSender sender = context.getSource().getSender();

                    LootListener.setDonationLevelOverride(null);
                    sender.sendMessage(configs.MESSAGE_CONFIG.get("lastlife.cmd.loot_level.reset", LootListener.getDonationLevel()));
                    return Command.SINGLE_SUCCESS;
                }))
                ;
    }
}
