package com.dragon.lastlife.donations;

import com.dragon.lastlife.loot.LootTier;
import com.dragon.lastlife.party.Party;
import com.dragon.lastlife.players.Participant;
import com.dragon.lastlife.utils.Utils;
import com.quiptmc.core.config.ConfigObject;
import com.quiptmc.core.config.objects.ConfigLocation;
import com.quiptmc.core.data.JsonSerializable;
import com.quiptmc.core.discord.embed.Embed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.loot.LootTable;
import org.json.JSONObject;

import java.awt.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Donation extends ConfigObject {

    public String displayName = "";
    public String donorId = "";
    public Links links = new Links();
    public boolean isRegFee = false;
    public int eventID = 0;
    public String createdDateUTC = "";
    public String recipientName = "";
    public String recipientImageURL = "";
    public int participantID = 0;
    public double amount = 0.0;
    public String avatarImageURL = "";
    public int teamID = 0;
    public String donationID = "";
    public String incentiveID = "";
    public String message = "";

    public Donation() {

    }

    public Donation(JSONObject json) {
        if(json.has("message") && json.isNull("message")) json.remove("message");
        this.fromJson(json);
    }

    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);
        super.id = donationID;
    }

    public ProcessResult<?> process() {
        //todo process donation and check incentives
        if(incentiveID != null && !incentiveID.isEmpty()) {
            Participant participant = Utils.configs().PARTICIPANT_CONFIG().get(participantID);
            if (participant != null) {
                if (incentiveID.equals(participant.incentive_life)) {
                    return new ProcessResult<>(IncentiveType.LIFE, participant);
                }
                if (incentiveID.equals(participant.incentive_boogey)) {
                    return new ProcessResult<>(IncentiveType.BOOGEYMAN, participant);
                }
                if (incentiveID.equals(participant.incentive_bundle_loot)) {
                    for(Party party : Utils.configs().PARTY_CONFIG().parties.values()){
                        LootTier tier = LootTier.of(Utils.configs().DONATION_CONFIG().total.doubleValue());
                        LootTable table = Bukkit.getLootTable(tier.key());
                        party.deliver(table);
                    }
                }
                if(incentiveID.equals(participant.incentive_shulker_loot)){
                    ConfigLocation configLocation = Utils.configs().POI_CONFIG().random();
                    Location location = new Location(Bukkit.getWorld(configLocation.world), configLocation.x, configLocation.y, configLocation.z);
                    LootTier tier = LootTier.of(Utils.configs().DONATION_CONFIG().total.doubleValue());
                    LootTable table = Bukkit.getLootTable(tier.key());
                    while(!location.getBlock().getType().isAir())
                        location.add(0, 1, 0);
                    location.getBlock().setType(Material.valueOf(DyeColor.values()[tier.value()+1 % DyeColor.values().length] + "_SHULKER_BOX"));
                    ShulkerBox shulkerBoxBlock = (ShulkerBox) location.getBlock().getState();
                    shulkerBoxBlock.setLootTable(table);
                    shulkerBoxBlock.update();
                    Utils.initializer().integration().log("DonationFlutter", "Shulker delivery to [" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + "]");

                }
            } else {
                Utils.initializer().integration().log("Donation", "Participant with ID " + participantID + " not found for donation incentive.");
            }
            return new ProcessResult<>(IncentiveType.NONE, participant);
        }
        return new ProcessResult<>(IncentiveType.NONE, participantID);

    }

    public Embed embed() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        String withSymbol = formatter.format(amount);
        Participant participant = Utils.configs().PARTICIPANT_CONFIG().get(participantID);

        String title = participant == null ? "New donation to an unknown participant" : "New donation to " + participant.player().getName();
        Embed.Field message = (this.message == null || this.message.isEmpty()) ? null : new Embed.Field("Message", this.message, false);
        String desc = "Amount: " + withSymbol + (isRegFee ? " (Registration Fee)" : "");
        String dateTimeString = createdDateUTC;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Embed.Builder builder = Embed.builder()
                .title(title)
                .description(desc)
                .thumbnail(recipientImageURL)
                .color(new Color(0xC02AE1));
        if (message != null)
            builder.field(message);
        try {
            Date date = sdf.parse(dateTimeString);
            builder.timestamp(date);
        } catch (Exception e) {
            Utils.initializer().getComponentLogger().error(Component.text("Error parsing donation date: " + dateTimeString, NamedTextColor.RED));
        }
        return builder.build();
    }

    public static class Links implements JsonSerializable {
        String recipient = "";
        String donate = "";

        public Links() {

        }

        public Links(JSONObject json) {
            this.fromJson(json);
        }
    }

    public record ProcessResult<T>(IncentiveType type, T payload) {

    }
}
