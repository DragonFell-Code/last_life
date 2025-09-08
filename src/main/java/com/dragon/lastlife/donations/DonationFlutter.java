package com.dragon.lastlife.donations;

import com.dragon.lastlife.config.DonationConfig;
import com.dragon.lastlife.utils.Utils;
import com.quiptmc.core.discord.WebhookManager;
import com.quiptmc.core.heartbeat.Flutter;
import com.quiptmc.core.utils.NetworkUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.http.HttpResponse;

public class DonationFlutter implements Flutter {

    private final int MAX_LIMIT = 100;
    private long LAST_HEARTBEAT = 0;
    private NetworkUtils.Get GET;
    private int offset = 0;


    public DonationFlutter(DonationConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("DonationConfig cannot be null");
        }
        GET = NetworkUtils.GET.defaults(config.etag);
    }

    private DonationConfig config() {
        return Utils.configs().DONATION_CONFIG();
    }

    @Override
    public boolean run() {
        long now = System.currentTimeMillis();
        if (now - LAST_HEARTBEAT >= config().seconds_per_check * 1000L) {
            LAST_HEARTBEAT = now;

            try {
                HttpResponse<String> response = NetworkUtils.get(GET, config().api_endpoint + "teams/" + config().team_id);
                if (response.statusCode() != 200 && response.statusCode() != 304) {
                    Utils.initializer().integration().log("DonationFlutter", "Failed to fetch team data: " + response.statusCode() + " - " + response.body());
                    return true;
                }
                if (response.statusCode() == 304 && offset <= 0) {
                    Utils.initializer().getLogger().config("No new donations available. Continuing...");
                    return true; // No new donations, continue running
                }
                String etag = response.headers().firstValue("etag").orElse("");
                if (!etag.equals(config().etag) || offset > 0) {
                    config().etag = etag;
                    JSONObject teamData = new JSONObject(response.body());
                    int allDonations = teamData.getInt("numDonations");
                    if (config().donations < allDonations) {
                        int diff = allDonations - config().donations;
                        Utils.initializer().integration().log("DonationFlutter", "New donations available: " + (diff));
                        if (diff > MAX_LIMIT) {
                            Utils.initializer().integration().log("DonationFlutter", "Too many new donations (" + diff + "), limiting to " + MAX_LIMIT);
                            if(offset != diff-MAX_LIMIT) offset = offset + (diff - MAX_LIMIT);
                            diff = MAX_LIMIT;
                        }

                        sync(diff);


                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return true; // Continue running
    }

    private void sync(int diff) {
        String url = config().api_endpoint + "teams/" + config().team_id + "/donations?limit=" + (diff) + (offset > 0 ? "&offset=" + (offset+1) : "");
        HttpResponse<String> donationsResponse = NetworkUtils.get(NetworkUtils.GET, url);
        if (donationsResponse.statusCode() != 200 && donationsResponse.statusCode() != 304) {
            Utils.initializer().integration().log("DonationFlutter", "Failed to fetch donations: " + donationsResponse.statusCode() + " - " + donationsResponse.body());
            return;
        }

        JSONArray donationsArray = new JSONArray(donationsResponse.body());
        JSONArray embedArray = new JSONArray();
        final int preOffset = offset;
        for (int i = 0; i < donationsArray.length(); i++) {
            JSONObject donationJson = donationsArray.getJSONObject(i);
            Donation donation = new Donation(donationJson);
            if (Utils.configs().DONATION_CONFIG().processed(donation)) {
                Utils.initializer().integration().warn("DonationFlutter", "Skipping already processed donation: " + donation.donationID);
                offset = offset + 1;
            } else {
                Utils.configs().DONATION_CONFIG().process(donation);
                embedArray.put(donation.embed().json());
            }

        }
        if(offset != preOffset){
            Utils.configs().DONATION_CONFIG().save();
        } else {
            offset = Math.max(offset - diff, 0);
        }
        if(WebhookManager.get("donations") != null){

            int batchSize = 10;
            int totalEmbeds = embedArray.length();
            int batches = (int) Math.ceil(totalEmbeds / (double) batchSize);

            for (int batchIndex = 0; batchIndex < batches; batchIndex++) {
                int startIndex = batchIndex * batchSize;
                int endIndex = Math.min(startIndex + batchSize, totalEmbeds);

                JSONArray batchArray = new JSONArray();
                for (int i = startIndex; i < endIndex; i++) {
                    batchArray.put(embedArray.getJSONObject(i));
                }

                if (batchArray.length() > 0) {
                    JSONObject send = new JSONObject();
                    send.put("embeds", batchArray);
                    WebhookManager.send("donations", send);
                }
            }


        }

        config().donations = config().donations + diff;
        Utils.initializer().integration().log("DonationFlutter", "Total donations: " + config().donations);
        config().save();
    }

}
