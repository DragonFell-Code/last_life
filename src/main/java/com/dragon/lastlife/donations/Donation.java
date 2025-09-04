package com.dragon.lastlife.donations;

import com.dragon.lastlife.players.Participant;
import com.dragon.lastlife.utils.Utils;
import com.quiptmc.core.config.ConfigObject;
import com.quiptmc.core.data.JsonSerializable;
import com.quiptmc.core.discord.Webhook;
import com.quiptmc.core.discord.WebhookManager;
import com.quiptmc.core.discord.embed.Embed;
import org.bukkit.Bukkit;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

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
        this.fromJson(json);
    }

    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);
        super.id = donationID;
    }

    public void process() {
        if (WebhookManager.get("donations") != null) {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
            String withSymbol = formatter.format(amount);
            Embed embed = new Embed()
                    .color(0xAF48EE)
                    .description("Amount: " + withSymbol + (isRegFee ? " (Registration Fee)" : ""))
                    .thumbnail(recipientImageURL);
            Participant participant = Utils.configs().PARTICIPANT_CONFIG().get(participantID);
            if (participant == null) {
                embed.title("New donation to an unknown participant");
            } else {
                embed.title("New donation to " + participant.player().getName());

            }
            if (!message.isEmpty())
                embed.addField("Message", message, false);
            Webhook webhook = WebhookManager.get("donations");
            WebhookManager.send(webhook, embed);
        }
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
}
