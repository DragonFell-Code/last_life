package com.dragon.lastlife.commands.executor;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.commands.CommandExecutor;
import com.dragon.lastlife.players.Participant;
import com.dragon.lastlife.utils.Utils;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;


public class BoogeyCommand extends CommandExecutor {


    public BoogeyCommand(Initializer initializer) {
        super(initializer, "boogey");
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> execute() {
        return literal(name())
                .executes(context -> showUsage(context, "lastlife.boogey"))
                .then(literal("set")
                        .executes(context -> {
                            CommandSender superSender = context.getSource().getSender();
                            if (!superSender.hasPermission("lastlife.boogey.set")) {
                                logError(superSender, "You do not have permission to use this command.");
                                return 0;
                            }
                            logError(superSender, "Usage: /boogey set <player> [true|false]");
                            return 1;
                        })
                        .then(argument("target", ArgumentTypes.players())
                                .executes(context -> {
                                    CommandSender superSender = context.getSource().getSender();
                                    if (!superSender.hasPermission("lastlife.boogey.set")) {
                                        logError(superSender, "You do not have permission to use this command.");
                                        return 0;
                                    }
                                    PlayerSelectorArgumentResolver targetResolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
                                    List<Player> targets = targetResolver.resolve(context.getSource());
                                    for (Player target : targets) {
                                        Participant participant = Utils.configs().PARTICIPANT_CONFIG().get(target.getUniqueId());
                                        if (participant == null) {
                                            logError(superSender, "Player " + target.getName() + " is not a participant.");
                                            continue;
                                        }
                                        participant.boogey = !participant.boogey;
                                        participant.sync();
                                        superSender.sendMessage(Utils.configs().MESSAGE_CONFIG.get("cmd.boogey.set", target.getName(), participant.boogey));
                                    }
                                    return 1;
                                })
                                .then(argument("value", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            String value;
                                            try {
                                                value = context.getArgument("value", String.class);
                                            }catch (IllegalArgumentException ex){
                                                value = "";
                                            }
                                            String[] values = {"true", "false"};
                                            if(value == null || value.isBlank()){
                                                for (String v : values) {
                                                    builder.suggest(v);
                                                }
                                                return builder.buildFuture();
                                            }
                                            for (String v : values) {
                                                if (v.startsWith(value.toLowerCase())) {
                                                    builder.suggest(v);
                                                }
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            CommandSender superSender = context.getSource().getSender();
                                            if (!superSender.hasPermission("lastlife.boogey.set")) {
                                                logError(superSender, "You do not have permission to use this command.");
                                                return 0;
                                            }
                                            boolean value = Boolean.parseBoolean(StringArgumentType.getString(context, "value"));
                                            PlayerSelectorArgumentResolver targetResolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
                                            List<Player> targets = targetResolver.resolve(context.getSource());
                                            for (Player target : targets) {
                                                Participant participant = Utils.configs().PARTICIPANT_CONFIG().get(target.getUniqueId());
                                                if (participant == null) {
                                                    logError(superSender, "Player " + target.getName() + " is not a participant.");
                                                    continue;
                                                }
                                                participant.boogey = value;
                                                participant.sync();
                                                superSender.sendMessage(Utils.configs().MESSAGE_CONFIG.get("cmd.boogey.set", target.getName(), participant.boogey));
                                            }
                                            return 1;
                                        }))))
                .then(literal("roll")
                        .executes(context -> {
                            CommandSender superSender = context.getSource().getSender();
                            if(!superSender.hasPermission("lastlife.boogey.roll")){
                                logError(superSender, "You do not have permission to use this command.");
                                return 0;
                            }
                            Utils.configs().PARTICIPANT_CONFIG().boogeymen().roll();
                            return 1;
                        })
                        .then(argument("amount", IntegerArgumentType.integer())
                                .executes(context -> {
                                    CommandSender superSender = context.getSource().getSender();
                                    if(!superSender.hasPermission("lastlife.boogey.roll")){
                                        logError(superSender, "You do not have permission to use this command.");
                                        return 0;
                                    }
                                    Utils.configs().PARTICIPANT_CONFIG().boogeymen().roll(context.getArgument("amount", Integer.class));
                                    return 1;
                                }))).build();
    }
}
