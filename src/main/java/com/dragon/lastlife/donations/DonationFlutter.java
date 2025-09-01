package com.dragon.lastlife.donations;

import com.dragon.lastlife.config.DonationConfig;
import com.dragon.lastlife.utils.Utils;
import com.quiptmc.core.heartbeat.Flutter;
import com.quiptmc.core.utils.NetworkUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.http.HttpResponse;

public class DonationFlutter implements Flutter {

    private final int MAX_LIMIT = 100;
    private final String API_ENDPOINT = "https://www.extra-life.org/api/";
    private final long LOOP_DELAY = 15000;
    private long LAST_HEARTBEAT = 0;
    private NetworkUtils.Get GET;

    public DonationFlutter(DonationConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("DonationConfig cannot be null");
        }
//        this.config = config;
        GET = NetworkUtils.GET.defaults(config.etag);
    }

    @Override
    public boolean run() {
        long now = System.currentTimeMillis();
        if (now - LAST_HEARTBEAT >= LOOP_DELAY) {
            System.out.println("Running DonationFlutter at " + now);
            LAST_HEARTBEAT = now;
            DonationConfig config = Utils.configs().DONATION_CONFIG();

            try {
                HttpResponse<String> response = NetworkUtils.get(GET, API_ENDPOINT + "teams/" + config.teamId);
                if (response.statusCode() != 200 && response.statusCode() != 304) {
                    Utils.initializer().integration().log("DonationFlutter", "Failed to fetch team data: " + response.statusCode() + " - " + response.body());
                    return true;
                }
                if (response.statusCode() == 304) {
                    Utils.initializer().getLogger().config("No new donations available. Continuing...");
                    return true; // No new donations, continue running
                }
                String etag = response.headers().firstValue("etag").orElse("");
                if (!etag.equals(config.etag)) {
                    //There has been a change in the donations, so we need to update the config
                    config.etag = etag;
                    JSONObject teamData = new JSONObject(response.body());
                    int allDonations = teamData.getInt("numDonations");
                    if (config.donations < allDonations) {
                        int diff = allDonations - config.donations;
                        System.out.println("New donations available: " + (diff));
                        if (diff > MAX_LIMIT) {
                            System.out.println("Limiting donations to " + MAX_LIMIT);
                            diff = MAX_LIMIT;
                        }
                        HttpResponse<String> donationsResponse = NetworkUtils.get(NetworkUtils.GET, API_ENDPOINT + "teams/" + config.teamId + "/donations?limit=" + diff);
                        if (donationsResponse.statusCode() != 200 && donationsResponse.statusCode() != 304) {
                            Utils.initializer().integration().log("DonationFlutter", "Failed to fetch donations: " + donationsResponse.statusCode() + " - " + donationsResponse.body());
                            return true;
                        }
//                        if (donationsResponse.statusCode() == 304) {
//                            Utils.initializer().getLogger().config("Failed to fetch donations: " + donationsResponse.statusCode() + " - " + donationsResponse.body());
//                            return true; // No new donations, continue running
//                        }
                        JSONArray donationsArray = new JSONArray(donationsResponse.body());
                        for (int i = 0; i < donationsArray.length(); i++) {
                            JSONObject donationJson = donationsArray.getJSONObject(i);
                            Donation donation = new Donation(donationJson);
//                            config.processed.put(donation);
//                            config.total += donation.amount;
                        }
                        //Loop thru donations and process them
                        config.donations = config.donations + diff;
                        System.out.println("Total donations: " + config.donations);
                        config.save();

                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return true; // Continue running
    }

}
