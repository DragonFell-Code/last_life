package com.dragon.lastlife.commands.executor;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.commands.CommandExecutor;
import com.dragon.lastlife.config.object.ConfigLocation;
import com.dragon.lastlife.donations.IncentiveType;
import com.dragon.lastlife.loot.LootTier;
import com.dragon.lastlife.loot.delivery.ShulkerDelivery;
import com.dragon.lastlife.party.Party;
import com.dragon.lastlife.players.Participant;
import com.dragon.lastlife.utils.Utils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Optional;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class DonationsCommand extends CommandExecutor {


    public DonationsCommand(Initializer initializer) {
        super(initializer, "donations");
    }

    public LiteralArgumentBuilder<CommandSourceStack> arguments() {
        return literal(name())
                .executes(context -> showUsage(context, ""))
                .then(literal("test")
                        .executes(a -> 1)
                        .then(literal("fox")
                                .executes(context -> {
                                    if (!(context.getSource().getSender() instanceof Player player))
                                        return logError(context, "Only players can link to participants.");
                                    if (!player.hasPermission("lastlife.admin"))
                                        return logError(context, "You do not have permission to use this command.");
                                    Participant participant = Utils.configs().PARTICIPANT_CONFIG().get(player.getUniqueId());
                                    if (participant.donorDriveId == 0)
                                        return logError(context, "You must link your Extra Life account first using /donations link <participantName>");
                                    Optional<Party> party = Utils.configs().PARTY_CONFIG().get(participant);
                                    if (party.isEmpty()) return logError(context, "You are not part of a party.");
                                    LootTier tier = LootTier.of(Utils.configs().DONATION_CONFIG().total.doubleValue());
                                    party.get().deliverFox(tier);
                                    player.sendMessage(Component.text("Delivered loot to party via fox!"));
                                    return 1;
                                }))
                        .then(literal("bundle")
                                .executes(context -> {
                                    if (!(context.getSource().getSender() instanceof Player player))
                                        return logError(context, "Only players can link to participants.");
                                    if (!player.hasPermission("lastlife.admin"))
                                        return logError(context, "You do not have permission to use this command.");
                                    Participant participant = Utils.configs().PARTICIPANT_CONFIG().get(player.getUniqueId());
                                    if (participant.donorDriveId == 0)
                                        return logError(context, "You must link your Extra Life account first using /donations link <participantName>");
                                    Optional<Party> party = Utils.configs().PARTY_CONFIG().get(participant);
                                    if (party.isEmpty()) return logError(context, "You are not part of a party.");
                                    LootTier tier = LootTier.of(Utils.configs().DONATION_CONFIG().total.doubleValue());
                                    party.get().deliverBundle(tier);
                                    player.sendMessage(Component.text("Delivered loot to party via magic!"));
                                    return 1;
                                }))
                        .then(literal("delivery").executes(context -> {
                            if (!(context.getSource().getSender() instanceof Player player))
                                return logError(context, "Only players can link to participants.");
                            if (!player.hasPermission("lastlife.admin"))
                                return logError(context, "You do not have permission to use this command.");
                            Participant participant = Utils.configs().PARTICIPANT_CONFIG().get(player.getUniqueId());
                            if (participant.donorDriveId == 0)
                                return logError(context, "You must link your Extra Life account first using /donations link <participantName>");
                            Optional<Party> party = Utils.configs().PARTY_CONFIG().get(participant);
                            if (party.isEmpty()) return logError(context, "You are not part of a party.");
                            LootTier tier = LootTier.of(Utils.configs().DONATION_CONFIG().total.doubleValue());
                            party.get().deliver(tier);
                            player.sendMessage(Component.text("Delivered loot to party via chance!"));
                            return 1;
                        }))
                        .then(literal("shulker").executes(context -> {
                            if (!context.getSource().getSender().hasPermission("lastlife.admin"))
                                return logError(context, "You do not have permission to use this command.");
                            ConfigLocation configLocation = Utils.configs().POI_CONFIG().random();
                            Location location = new Location(Bukkit.getWorld(configLocation.world), configLocation.x, configLocation.y, configLocation.z);
                            while (!location.getBlock().getType().isAir()) location.add(0, 1, 0);
                            LootTier tier = LootTier.of(Utils.configs().DONATION_CONFIG().total.doubleValue());
                            new ShulkerDelivery(location, tier).start();
                            context.getSource().getSender().sendMessage(Component.text("Delivered loot to a random location!"));
                            return 1;
                        })))
                .then(literal("incentives").executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            if (!(sender instanceof Player player))
                                return logError(context, "Only players can link to participants.");
                            if (Utils.configs().PARTICIPANT_CONFIG().get(player.getUniqueId()).donorDriveId == 0)
                                return logError(context, "You must link your Extra Life account first using /donations link <participantName>");
                            return showUsage(context, "");
                        })
                        .then(argument("type", StringArgumentType.word()).executes(context -> {
                                    CommandSender sender = context.getSource().getSender();
                                    if (!(sender instanceof Player player))
                                        return logError(context, "Only players can link to participants.");
                                    if (Utils.configs().PARTICIPANT_CONFIG().get(player.getUniqueId()).donorDriveId == 0)
                                        return logError(context, "You must link your Extra Life account first using /donations link <participantName>");
                                    return showUsage(context, "");
                                })
                                .suggests(((context, builder) -> {
                                    String[] types = new String[IncentiveType.values().length];
                                    for (int i = 0; i < types.length; i++) {
                                        IncentiveType type = IncentiveType.values()[i];
                                        if(type == IncentiveType.NONE)continue;
                                        types[i] = type.name().toLowerCase();
                                    }
                                    return onlySimilar(types, "type", context, builder);
                                }))
                                .then(argument("incentiveName", StringArgumentType.greedyString()).suggests((context, builder) -> {
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

                                            JSONArray raw = participant.getIncentives();
                                            String[] values = new String[raw.length()];
                                            for (int i = 0; i < raw.length(); i++) {
                                                JSONObject participantJson = raw.getJSONObject(i);
                                                values[i] = participantJson.getString("description");
                                            }
                                            return onlySimilar(values, "incentiveName", context, builder);
                                        })
                                        .executes(context -> {
                                            String typeArg = StringArgumentType.getString(context, "type").toUpperCase();
                                            IncentiveType type;
                                            try {
                                                type = IncentiveType.valueOf(typeArg);
                                            } catch (IllegalArgumentException e) {
                                                return logError(context, "Type must be one of life, boogey, shulker_loot or bundle_loot.");
                                            }
                                            CommandSender sender = context.getSource().getSender();
                                            if (!(sender instanceof Player player)) {
                                                return logError(context, "Only players can link to participants.");
                                            }
                                            Participant participant = Utils.configs().PARTICIPANT_CONFIG().get(player.getUniqueId());
                                            if (participant.donorDriveId == 0) {
                                                return logError(context, "You must link your Extra Life account first using /donations link <participantName>");
                                            }

                                            JSONArray raw = participant.getIncentives(true);
                                            for (int i = 0; i < raw.length(); i++) {
                                                JSONObject incentive = raw.getJSONObject(i);
                                                if (incentive.has("description") && incentive.getString("description").equalsIgnoreCase(StringArgumentType.getString(context, "incentiveName"))) {
                                                    switch (type) {
                                                        case LIFE ->
                                                                participant.incentive_life = incentive.getString("incentiveID");
                                                        case BOOGEYMAN ->
                                                                participant.incentive_boogey = incentive.getString("incentiveID");
                                                        case SHULKER_LOOT ->
                                                                participant.incentive_shulker_loot = incentive.getString("incentiveID");
                                                        case BUNDLE_LOOT ->
                                                                participant.incentive_bundle_loot = incentive.getString("incentiveID");
                                                    }
                                                    Utils.configs().PARTICIPANT_CONFIG().save();
                                                    player.sendMessage(Component.text("Successfully set your " + typeArg + " incentive to " + incentive.getString("description") + " (ID: " + incentive.getString("incentiveID") + ")", NamedTextColor.GREEN));
                                                    return 1;
                                                }
                                            }
                                            return logError(context, "Could not find incentive with name " + StringArgumentType.getString(context, "incentiveName"));
                                        }))))

                .then(literal("link")
                        .executes(context -> showUsage(context, ""))
                        .then(argument("participantName", StringArgumentType.greedyString())
                                .suggests((context, builder) -> {
                                    JSONArray raw = Utils.configs().PARTICIPANT_CONFIG().fetchParticipants();
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
                                    JSONArray raw = Utils.configs().PARTICIPANT_CONFIG().fetchParticipants(true);
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
                                })));
    }
}
