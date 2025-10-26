package com.dragon.lastlife.commands.executor;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.commands.CommandExecutor;
import com.dragon.lastlife.players.Participant;
import com.dragon.lastlife.utils.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.entity.Player;

import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class LifeCommand extends CommandExecutor {
    public LifeCommand(Initializer initializer) {
        super(initializer, "lives");
    }
    @Override
    public LiteralCommandNode<CommandSourceStack> execute() {
        return literal(name()).executes((context) -> {
            if (!context.getSource().getSender().hasPermission("lastlife.admin"))
                return logError(context, "You do not have permission to use this command.");
            return showUsage(context, "lastlife.admin");
        }).then(argument("target", ArgumentTypes.player()).executes(context -> {
            if (!context.getSource().getSender().hasPermission("lastlife.admin.life.view"))
                return logError(context, "You do not have permission to use this command.");
            PlayerSelectorArgumentResolver targetResolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
            List<Player> targets = targetResolver.resolve(context.getSource());
            for (Player target : targets) {
                Participant participant = Utils.configs().PARTICIPANT_CONFIG().get(target.getUniqueId());
                context.getSource().getSender().sendMessage(Utils.configs().MESSAGE_CONFIG.get("lastlife.cmd.life.view", target.getName(), participant.lives().get()));
            }
            return Command.SINGLE_SUCCESS;
        }).then(argument("action", StringArgumentType.word()).suggests((context, builder) -> onlySimilar(new String[]{"add", "remove", "set"}, "action", context, builder)).executes(context -> {
            String action = StringArgumentType.getString(context, "action").toLowerCase();
            if (!context.getSource().getSender().hasPermission("lastlife.admin.life." + action))
                return logError(context, "You do not have permission to use this command.");
            return showUsage(context, "lastlife.admin.life." + action);
        }).then(argument("amount", IntegerArgumentType.integer(1, 10)).executes(context -> {
            String action = StringArgumentType.getString(context, "action").toLowerCase();
            int amount = IntegerArgumentType.getInteger(context, "amount");
            PlayerSelectorArgumentResolver targetResolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
            if (!context.getSource().getSender().hasPermission("lastlife.admin.life." + action))
                return logError(context, "You do not have permission to use this command.");
            if (action.equalsIgnoreCase("add") || action.equalsIgnoreCase("remove")) {
                if (action.equalsIgnoreCase("remove") && amount >= 1) amount = amount * -1;
                List<Player> targets = targetResolver.resolve(context.getSource());
                for (Player target : targets) {
                    Participant participant = Utils.configs().PARTICIPANT_CONFIG().get(target.getUniqueId());
                    participant.lives().add(amount);
                    context.getSource().getSender().sendMessage(Utils.configs().MESSAGE_CONFIG.get("lastlife.cmd.life.set", target.getName(), participant.lives().get()));
                }
                return Command.SINGLE_SUCCESS;
            }
            if (action.equalsIgnoreCase("set")) {
                List<Player> targets = targetResolver.resolve(context.getSource());
                for (Player target : targets) {
                    Participant participant = Utils.configs().PARTICIPANT_CONFIG().get(target.getUniqueId());
                    participant.lives().set(amount);
                    context.getSource().getSender().sendMessage(Utils.configs().MESSAGE_CONFIG.get("lastlife.cmd.life.set", target.getName(), participant.lives().get()));
                }
                return Command.SINGLE_SUCCESS;
            }
            return logError(context, "Invalid action specified.");
        })))).build();
    }
}
