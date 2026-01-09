package com.dragon.lastlife.donations;

import com.dragon.lastlife.config.DonationConfig;
import com.dragon.lastlife.config.object.ConfigLocation;
import com.dragon.lastlife.loot.delivery.ShulkerDelivery;
import com.dragon.lastlife.party.Party;
import com.dragon.lastlife.players.Participant;
import com.dragon.lastlife.utils.Utils;
import com.quiptmc.core.discord.WebhookManager;
import com.quiptmc.core.heartbeat.Flutter;
import com.quiptmc.core.utils.TaskScheduler;
import com.quiptmc.core.utils.net.HttpConfig;
import com.quiptmc.core.utils.net.HttpHeaders;
import com.quiptmc.core.utils.net.NetworkUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DonationFlutter implements Flutter {

    public static final int MAX_LIMIT = 100;

    private HttpConfig GET;
    private long LAST_HEARTBEAT = 0;
    private boolean validated = false;

    public DonationFlutter(DonationConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("DonationConfig cannot be null");
        }
        etag(config.etag);
        Utils.initializer().integration().log("DonationFlutter", "Initialized. Validating all donations.");

        validateDonations();

    }

    public void refresh(){
        etag("null");
        LAST_HEARTBEAT = 0;
    }

    public void etag(String etag){
        GET = HttpConfig.defaults(HttpHeaders.ETAG(etag));
    }

    public void validateDonations() {
        validated = false;
        HttpResponse<String> teamResponse = NetworkUtils.get(HttpConfig.DEFAULTS, config().api_endpoint + "teams/" + config().team_id + "/");
        int numDonations;
        try {
            JSONObject teamData = new JSONObject(teamResponse.body());
            numDonations = teamData.getInt("numDonations");
        } catch (JSONException ex) {
            Utils.initializer().integration().log("DonationFlutter", "Could not validate donations, invalid API response: " + teamResponse.body());
            Utils.initializer().integration().logger().error("DonationFlutter", ex);
            return;
        }
        TaskScheduler.scheduleAsyncTask(() -> validateLoop(numDonations, 0), config().seconds_per_check, TimeUnit.SECONDS);
    }

    private void validateLoop(int numDonations, int overdraw) {
        String url = config().api_endpoint + "teams/" + config().team_id + "/donations?limit=100" + (overdraw > 0 ? "&offset=" + (overdraw) : "");
        HttpResponse<String> donationsResponse = NetworkUtils.get(NetworkUtils.DEFAULT, url);
        JSONArray donationsJson = new JSONArray(donationsResponse.body());
        int batchSize = donationsJson.length();
        overdraw = overdraw + batchSize;
        numDonations = numDonations - batchSize;
        Utils.initializer().integration().log("DonationFlutter DEBUG", "Batch size: " + batchSize);
        Utils.initializer().integration().log("DonationFlutter DEBUG", "New Overdraw: " + overdraw);
        Utils.initializer().integration().log("DonationFlutter DEBUG", "New NumDonations: " + numDonations);

        processArray(donationsJson);

        if (numDonations > 0) {
            Utils.initializer().integration().log("DonationFlutter DEBUG", "Validation Incomplete. Continuing...");
            int finalNumDonations = numDonations;
            int finalOverdraw = overdraw;
            TaskScheduler.scheduleAsyncTask(() -> validateLoop(finalNumDonations, finalOverdraw), config().seconds_per_check, TimeUnit.SECONDS);
        } else {
            config().save();
            validated = true;
            Utils.initializer().integration().log("DonationFlutter", "Validation Complete");
            Bukkit.broadcast(Component.text("Donation validation complete!", NamedTextColor.GREEN), "lastlife.admin");
        }
    }

    private List<Donation.ProcessResult<?>> processArray(JSONArray donationsArray) {
        List<Donation.ProcessResult<?>> results = new ArrayList<>();
        for (int i = 0; i < donationsArray.length(); i++) {
            JSONObject donationJson = donationsArray.getJSONObject(i);
            Donation donation = new Donation(donationJson);

            if (Utils.configs().DONATION_CONFIG().processed(donation)) {
                Utils.initializer().integration().warn("DonationFlutter", "Skipping already processed donation: " + donation.donationID);
            } else {
                Donation.ProcessResult<?> result = Utils.configs().DONATION_CONFIG().process(donation);
                Utils.initializer().integration().log("DonationFlutter", "Processed donation: " + donation.donationID);
                results.add(result);
            }


        }
        return results;
    }

    private void processLoop() {
        Instant instant = Instant.ofEpochMilli(config().last_bucket);

        // The default toString() format of Instant is an ISO-8601 UTC string (e.g., 2024-02-20T16:00:26.969Z)
        String utcString = instant.toString();
        String url = config().api_endpoint + "teams/" + config().team_id + "/donations?where=" + URLEncoder.encode("createdDateUTC >= '" + utcString + "'", StandardCharsets.UTF_8);
        HttpResponse<String> donationsResponse = NetworkUtils.get(NetworkUtils.DEFAULT, url);
        JSONArray donationsJson = new JSONArray(donationsResponse.body());
        int batchSize = donationsJson.length();
        if (batchSize >= 100)
            Utils.initializer().integration().warn("DonationFlutter", "Donations API returned more than 100 donations. This is not normal. Please report this to the developer.");

        List<Donation.ProcessResult<?>> results = processArray(donationsJson);
        Utils.initializer().integration().log("DonationFlutter", "Batch of " + batchSize + " new donations. Processed " + results.size() + " donations.");
        if (results.isEmpty()) {
            Utils.initializer().integration().warn("DonationFlutter", "processLoop was run but no new donations were found.");
            return;
        }

        finalizeResults(results);



        Utils.initializer().integration().log("DonationFlutter", "Total donations: " + config().donations);
        config().save();
    }

    private void finalizeResults(List<Donation.ProcessResult<?>> results){
        Bukkit.getScheduler().runTask(Utils.initializer(), () -> handleResults(results));

        if (WebhookManager.get("donations") != null) {
            int webhookBatchSize = 10;
            int totalEmbeds = results.size();
            int batches = (int) Math.ceil(totalEmbeds / (double) webhookBatchSize);

            for (int batchIndex = 0; batchIndex < batches; batchIndex++) {
                int startIndex = batchIndex * webhookBatchSize;
                int endIndex = Math.min(startIndex + webhookBatchSize, totalEmbeds);

                JSONArray batchArray = new JSONArray();
                for (int i = startIndex; i < endIndex; i++) {
                    batchArray.put(results.get(i).embed().json());
                }

                if (!batchArray.isEmpty()) {
                    JSONObject send = new JSONObject();
                    send.put("embeds", batchArray);
                    WebhookManager.send("donations", send);
                }
            }
        }
    }

    private DonationConfig config() {
        return Utils.configs().DONATION_CONFIG();
    }

    @Override
    public boolean run() {
        if (!validated) return true;
        long now = System.currentTimeMillis();
        if (now - LAST_HEARTBEAT <= config().seconds_per_check * 1000L) return true;
        LAST_HEARTBEAT = now;

        try {
            HttpResponse<String> response = NetworkUtils.get(GET, config().api_endpoint + "teams/" + config().team_id);
            if (response.statusCode() != 200 && response.statusCode() != 304) {
                Utils.initializer().integration().log("DonationFlutter", "Failed to fetch team data: " + response.statusCode() + " - " + response.body());
                return true;
            }
            if (response.statusCode() == 304) {
                Utils.initializer().getLogger().config("No new donations available. Continuing...");
                return true; // No new donations continue running
            }
            String etag = response.headers().firstValue("etag").orElse("");
            if (etag.equals(config().etag)) return true;
            etag(etag);
            JSONObject teamData = new JSONObject(response.body());
            int allDonations = teamData.getInt("numDonations");
            if (config().donations >= allDonations) return true;
            int diff = allDonations - config().donations;
            Utils.initializer().integration().log("DonationFlutter", "New donations available: " + (diff));
            processLoop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return true; // Continue running
    }

//    private void sync(int diff) {
//        String url = config().api_endpoint + "teams/" + config().team_id + "/donations?limit=" + (diff) + (offset > 0 ? "&offset=" + (offset + 1) : "");
//        HttpResponse<String> donationsResponse = NetworkUtils.get(NetworkUtils.DEFAULT, url);
//        if (donationsResponse.statusCode() != 200 && donationsResponse.statusCode() != 304) {
//            Utils.initializer().integration().log("DonationFlutter", "Failed to fetch donations: " + donationsResponse.statusCode() + " - " + donationsResponse.body());
//            return;
//        }
//
//        JSONArray donationsArray = new JSONArray(donationsResponse.body());
//        List<Donation.ProcessResult<?>> results = processArray(donationsArray);
//
//        if (!results.isEmpty()) {
//            Utils.configs().DONATION_CONFIG().save();
//        } else {
//
//        }
//
//        Bukkit.getScheduler().runTask(Utils.initializer(), () -> handleResults(results));
//
//        if (WebhookManager.get("donations") != null) {
//            int batchSize = 10;
//            int totalEmbeds = embedArray.length();
//            int batches = (int) Math.ceil(totalEmbeds / (double) batchSize);
//
//            for (int batchIndex = 0; batchIndex < batches; batchIndex++) {
//                int startIndex = batchIndex * batchSize;
//                int endIndex = Math.min(startIndex + batchSize, totalEmbeds);
//
//                JSONArray batchArray = new JSONArray();
//                for (int i = startIndex; i < endIndex; i++) {
//                    batchArray.put(embedArray.getJSONObject(i));
//                }
//
//                if (!batchArray.isEmpty()) {
//                    JSONObject send = new JSONObject();
//                    send.put("embeds", batchArray);
//                    WebhookManager.send("donations", send);
//                }
//            }
//        }
//
//        config().donations = config().donations + diff;
//        Utils.initializer().integration().log("DonationFlutter", "Total donations: " + config().donations);
//        config().save();
//    }

    private void handleResults(List<Donation.ProcessResult<?>> results) {
        Map<Participant, Integer> lifeMap = new HashMap<>();

        if (!results.isEmpty()) {
            Utils.initializer().integration().log("DonationFlutter", "Processed " + results.size() + " new donations.");
            for (Donation.ProcessResult<?> result : results) {
                Participant participant = (Participant) result.payload();
                switch (result.type()) {
                    case LIFE -> {
                        // Don't give a life if player is already Dead
                        if (participant.lives().lives() > 0 && participant.lives().lives() < 3) {
                            lifeMap.put(participant, lifeMap.getOrDefault(participant, 0) + 1);
                        }
                    }
                    case BUNDLE_LOOT -> {
                        Utils.genericWebhook("donations", new Color(0xE26922), "Bundle Delivery", null, "A donation to " + participant.player().getName() + " has delivered a bundle of loot!");
                        for (Party party : Utils.configs().PARTY_CONFIG().parties.values()) {
                            party.deliver(participant);
                        }
                    }
                    case SHULKER_LOOT -> {
                        ConfigLocation configLocation = Utils.configs().POI_CONFIG().random();
                        Location location = new Location(Bukkit.getWorld(configLocation.world), configLocation.x, configLocation.y, configLocation.z);

                        while (!location.getBlock().getType().isAir()) location.add(0, 1, 0);
                        new ShulkerDelivery(location).start();
                        String msg = "A donation to " + participant.player().getName() + " has spawned a shulker delivery at the " + configLocation.id() + " POI!";
                        Utils.genericWebhook("donations", new Color(0x1471A5), "Shulker Delivery", null, msg);
                        Bukkit.broadcast(Component.text(msg, NamedTextColor.GREEN));
                    }
                    case BOOGEYMAN -> {
                        Utils.configs().PARTICIPANT_CONFIG().boogeymen().queue();
                        Utils.genericWebhook("boogey", new Color(0xFFD738), "Boogeyman Queue", null, "A donation to " + participant.player().getName() + " has added 1 participant to the boogeyman queue!");
                        Utils.initializer().integration().log("Donation", participant.player().getName() + " received a donation on their boogeyman incentive.");
                    }
                }
            }
            for (Map.Entry<Participant, Integer> entry : lifeMap.entrySet()) {
                entry.getKey().lives().add(entry.getValue());
                Utils.genericWebhook("donations", new Color(0x85FF00), "Lives", null, entry.getKey().player().getName() + " has received " + entry.getValue() + " extra life" + (entry.getValue() > 1 ? "s" : "") + " from donations! They now have " + entry.getKey().lives().get() + " life" + (entry.getKey().lives().get() > 1 ? "s" : "") + ".");
            }
            Utils.configs().PARTICIPANT_CONFIG().save();
        }
        Utils.configs().DUNGEON_MANAGER.handleNewDonationTotal(true);
    }
}
