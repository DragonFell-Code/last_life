package com.dragon.lastlife.commands.executor;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.commands.CommandExecutor;
import com.dragon.lastlife.players.Participant;
import com.dragon.lastlife.utils.Utils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.quiptmc.core.config.objects.ConfigString;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class SettingsCommand extends CommandExecutor {

    public SettingsCommand(Initializer initializer) {
        super(initializer, "settings");
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> arguments() {
        return literal(name())
                .executes(context -> showUsage(context, "lastlife.settings"))
                .then(literal("view")
                        .executes(context -> showUsage(context, "lastlife.settings"))
                        .then(argument("key", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    // Suggest existing setting keys for the executing player
                                    if (context.getSource().getSender() instanceof Player player) {
                                        Participant p = Utils.configs().PARTICIPANT_CONFIG().get(player.getUniqueId());
                                        String entered;
                                        try {
                                            entered = context.getArgument("key", String.class);
                                        } catch (IllegalArgumentException ex) {
                                            entered = "";
                                        }
                                        for (var cfg : p.settings.values()) {
                                            String k = cfg.id();
                                            if (k != null && k.toLowerCase().contains(entered.toLowerCase()))
                                                builder.suggest(k);
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context->{
                                    if (!(context.getSource().getSender() instanceof Player player))
                                        return logError(context, "Only players can change their settings.");
                                    if (!player.hasPermission("lastlife.settings"))
                                        return logError(context, "You do not have permission to use this command.");

                                    String key = StringArgumentType.getString(context, "key");
                                    Participant participant = Utils.configs().PARTICIPANT_CONFIG().get(player.getUniqueId());
                                    if (participant == null)
                                        return logError(context, "You are not registered in the game.");
                                    if (!participant.settings.contains(key)) {
                                        return logError(context, "Unknown setting: " + key);
                                    }
                                    player.sendMessage(Utils.configs().MESSAGE_CONFIG.get("lastlife.settings.view", key, participant.settings.get(key).value()));
                                    return 1;
                                })))
                .then(literal("set")
                        .executes(context -> showUsage(context, "lastlife.settings"))
                        .then(argument("key", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    // Suggest existing setting keys for the executing player
                                    if (context.getSource().getSender() instanceof Player player) {
                                        Participant p = Utils.configs().PARTICIPANT_CONFIG().get(player.getUniqueId());
                                        String entered;
                                        try {
                                            entered = context.getArgument("key", String.class);
                                        } catch (IllegalArgumentException ex) {
                                            entered = "";
                                        }
                                        for (var cfg : p.settings.values()) {
                                            String k = cfg.id();
                                            if (k != null && k.toLowerCase().contains(entered.toLowerCase()))
                                                builder.suggest(k);
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> showUsage(context, "lastlife.settings"))
                                .then(argument("value", StringArgumentType.word())
                                        .suggests((context, builder) -> onlySimilar(new String[]{"true", "false"}, "value", context, builder))
                                        .executes(context -> {
                                            if (!(context.getSource().getSender() instanceof Player player))
                                                return logError(context, "Only players can change their settings.");
                                            if (!player.hasPermission("lastlife.settings"))
                                                return logError(context, "You do not have permission to use this command.");

                                            String key = StringArgumentType.getString(context, "key");
                                            String value = StringArgumentType.getString(context, "value");
                                            String lc = value.toLowerCase();
                                            if (!lc.equals("true") && !lc.equals("false")) {
                                                return logError(context, "Value must be either 'true' or 'false'.");
                                            }

                                            Participant participant = Utils.configs().PARTICIPANT_CONFIG().get(player.getUniqueId());
                                            if (!participant.settings.contains(key)) {
                                                return logError(context, "Unknown setting: " + key);
                                            }
                                            // Replace the config string value by re-putting the entry
                                            participant.settings.remove(key);
                                            participant.settings.put(new ConfigString(key, lc));
                                            Utils.configs().PARTICIPANT_CONFIG().cache.put(participant);
                                            Utils.configs().PARTICIPANT_CONFIG().save();
                                            participant.sync();
                                            player.sendMessage(Utils.configs().MESSAGE_CONFIG.get("lastlife.settings.set", key, lc));
                                            return 1;
                                        }))));
    }
}
