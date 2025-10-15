package com.dragon.lastlife.commands.executor;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.commands.CommandExecutor;
import com.dragon.lastlife.players.Participant;
import com.dragon.lastlife.utils.Utils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.quiptmc.core.utils.net.NetworkUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.http.HttpResponse;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class DonationsCommand extends CommandExecutor {


    public DonationsCommand(Initializer initializer) {
        super(initializer, "donations");
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> execute() {
        return literal(name())
                .executes(context -> showUsage(context, ""))
                .then(literal("incentives")
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            if (!(sender instanceof Player player))
                                return logError(context, "Only players can link to participants.");
                            if (Utils.configs().PARTICIPANT_CONFIG().get(player.getUniqueId()).donorDriveId == 0)
                                return logError(context, "You must link your Extra Life account first using /donations link <participantName>");
                            return logError(context, "Usage: /donations incentives <life|boogey|loot> <incentiveName>");
                        })
                        .then(argument("type", StringArgumentType.word())
                                .executes(context -> {
                                    CommandSender sender = context.getSource().getSender();
                                    if (!(sender instanceof Player player))
                                        return logError(context, "Only players can link to participants.");
                                    if (Utils.configs().PARTICIPANT_CONFIG().get(player.getUniqueId()).donorDriveId == 0)
                                        return logError(context, "You must link your Extra Life account first using /donations link <participantName>");
                                    return logError(context, "Usage: /donations incentives life <incentiveName>");
                                })
                                .suggests(((context, builder) -> {
                                    String[] types = new String[]{"life", "boogey", "loot"};
                                    return onlySimilar(types, "type", context, builder);
                                }))
                                .then(argument("incentiveName", StringArgumentType.greedyString())
                                        .suggests((context, builder) -> {
                                            CommandSender sender = context.getSource().getSender();
                                            if (!(sender instanceof Player player)) {
                                                logError(context, "Only players can link to participants.");
                                                return builder.buildFuture();
                                            }
                                            Participant participant = Utils.configs().PARTICIPANT_CONFIG().get(player.getUniqueId());
                                            if (participant.donorDriveId == 0) {
                                                logError(context, "You must link your Extra Life account first using /donations link <participantName>");
                                                return builder.buildFuture();
                                            }

                                            HttpResponse<String> response = NetworkUtils.get(NetworkUtils.DEFAULT, Utils.configs().DONATION_CONFIG().api_endpoint + "participants/" + participant.donorDriveId + "/incentives");
                                            JSONArray raw = new JSONArray(response.body());
                                            String[] values = new String[raw.length()];
                                            for (int i = 0; i < raw.length(); i++) {
                                                JSONObject participantJson = raw.getJSONObject(i);
                                                values[i] = participantJson.getString("description");
                                            }
                                            return onlySimilar(values, "incentiveName", context, builder);
                                        })
                                        .executes(context -> {
                                            String type = StringArgumentType.getString(context, "type").toLowerCase();
                                            if (!type.equals("life") && !type.equals("boogey") && !type.equals("loot")) {
                                                return logError(context, "Type must be one of life, boogey, or loot.");
                                            }
                                            CommandSender sender = context.getSource().getSender();
                                            if (!(sender instanceof Player player)) {
                                                return logError(context, "Only players can link to participants.");
                                            }
                                            Participant participant = Utils.configs().PARTICIPANT_CONFIG().get(player.getUniqueId());
                                            if (participant.donorDriveId == 0) {
                                                return logError(context, "You must link your Extra Life account first using /donations link <participantName>");
                                            }

                                            HttpResponse<String> response = NetworkUtils.get(NetworkUtils.DEFAULT, Utils.configs().DONATION_CONFIG().api_endpoint + "participants/" + participant.donorDriveId + "/incentives");
                                            JSONArray raw = new JSONArray(response.body());
                                            for (int i = 0; i < raw.length(); i++) {
                                                JSONObject incentive = raw.getJSONObject(i);
                                                if (incentive.has("description") && incentive.getString("description").equalsIgnoreCase(StringArgumentType.getString(context, "incentiveName"))) {
                                                    switch (type) {
                                                        case "life" -> participant.incentive_life = incentive.getString("incentiveID");
                                                        case "boogey" -> participant.incentive_boogey = incentive.getString("incentiveID");
                                                        case "loot" -> participant.incentive_loot = incentive.getString("incentiveID");
                                                    }
                                                    Utils.configs().PARTICIPANT_CONFIG().save();
                                                    player.sendMessage(Component.text("Successfully set your " + type + " incentive to " + incentive.getString("description") + " (ID: " + incentive.getString("incentiveID") + ")", NamedTextColor.GREEN));
                                                    return 1;
                                                }
                                            }
                                            return logError(context, "Could not find incentive with name " + StringArgumentType.getString(context, "incentiveName"));
                                        }))))

                .then(literal("link")
                        .executes(context -> logError(context, "Usage: /donations link <participantName>"))
                        .then(argument("participantName", StringArgumentType.greedyString())
                                .suggests((context, builder) -> {
                                    HttpResponse<String> response = NetworkUtils.get(NetworkUtils.DEFAULT, Utils.configs().DONATION_CONFIG().api_endpoint + "teams/" + Utils.configs().DONATION_CONFIG().team_id + "/participants");
                                    JSONArray raw = new JSONArray(response.body());
                                    String[] values = new String[raw.length()];
                                    for (int i = 0; i < raw.length(); i++) {
                                        JSONObject participant = raw.getJSONObject(i);
                                        values[i] = participant.getString("displayName");
                                    }
                                    return onlySimilar(values, "participantName", context, builder);
                                })
                                .executes(context -> {
                                    CommandSender sender = context.getSource().getSender();
                                    if (!(sender instanceof Player player))
                                        return logError(context, "Only players can link to participants.");
                                    HttpResponse<String> response = NetworkUtils.get(NetworkUtils.DEFAULT, Utils.configs().DONATION_CONFIG().api_endpoint + "teams/" + Utils.configs().DONATION_CONFIG().team_id + "/participants");
                                    JSONArray raw = new JSONArray(response.body());
                                    String participantName = StringArgumentType.getString(context, "participantName");
                                    for (int i = 0; i < raw.length(); i++) {
                                        JSONObject participant = raw.getJSONObject(i);
                                        if (participant.getString("displayName").equalsIgnoreCase(participantName)) {
                                            long participantId = participant.getLong("participantID");
                                            Utils.configs().PARTICIPANT_CONFIG().link(player.getUniqueId(), participantId);
                                            return logError(context, "Successfully linked " + context.getSource().getSender().getName() + " to Extra Life participant " + participantName + " (ID: " + participantId + ")");
                                        }
                                    }
                                    return logError(context, "Could not find participant with name " + participantName);
                                })))
                .build();
    }
}
