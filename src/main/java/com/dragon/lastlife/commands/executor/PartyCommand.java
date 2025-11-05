package com.dragon.lastlife.commands.executor;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.commands.CommandExecutor;
import com.dragon.lastlife.commands.executor.argument.PartyArgumentType;
import com.dragon.lastlife.party.Party;
import com.dragon.lastlife.players.Participant;
import com.dragon.lastlife.utils.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.quiptmc.core.config.Config;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.apache.commons.lang3.ObjectUtils;
import org.bukkit.entity.Player;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;


public class PartyCommand extends CommandExecutor {

    public PartyCommand(Initializer initializer) {
        super(initializer, "party");
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> execute() {
        return arguments().build();
    }

    public LiteralArgumentBuilder<CommandSourceStack> arguments() {
        return literal(name())
                .executes(context -> showUsage(context, "lastlife.party"))
                .then(literal("create")
                        .executes(context -> showUsage(context, "lastlife.party.create"))
                        .then(argument("partyName", StringArgumentType.word()))
                        .executes(context -> {
                            if (!context.getSource().getSender().hasPermission("lastlife.party.create"))
                                return logError(context, "You do not have permission to use this command.");
                            String partyName = StringArgumentType.getString(context, "partyName");
                            Utils.configs().PARTY_CONFIG().create(partyName);
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(literal("leave")
                        .executes(context -> {
                            if (!context.getSource().getSender().hasPermission("lastlife.party.leave"))
                                return logError(context, "You do not have permission to use this command.");
                            if(!(context.getSource().getSender() instanceof Player target))
                                return logError(context, "Only players can leave parties.");
                            Participant participant = Utils.configs().PARTICIPANT_CONFIG().get(target.getUniqueId());
                            if(participant == null)
                                return logError(context, "You are not linked to a participant.");
                            Party party = Utils.configs().PARTY_CONFIG().get(participant).orElse(null);
                            if(party == null)
                                return logError(context, "You are not in a party.");
                            party.leave(participant);
                            return Command.SINGLE_SUCCESS;

                        }))
                .then(literal("join")
                        .executes(context -> showUsage(context, "lastlife.party.join"))
                        .then(argument("partyName", new PartyArgumentType())
                                .executes(context -> {
                                    if (!context.getSource().getSender().hasPermission("lastlife.party.join"))
                                        return logError(context, "You do not have permission to use this command.");
                                    if(!(context.getSource().getSender() instanceof Player target))
                                        return logError(context, "Only players can join parties.");
                                    try {
                                        Party party = context.getArgument("partyName", Party.class);
                                        Participant participant = Utils.configs().PARTICIPANT_CONFIG().get(target.getUniqueId());
                                        if (participant == null)
                                            return logError(context, "You must link your account to a participant first.");
                                        party.join(participant);
                                    }catch (NullPointerException e){
                                        return logError(context, "Party not found.");
                                    }
                                    return 0;
                                })));
    }
}
