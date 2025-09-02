package com.dragon.lastlife.config;

import com.dragon.lastlife.donations.Donation;
import com.quiptmc.core.QuiptIntegration;
import com.quiptmc.core.config.Config;
import com.quiptmc.core.config.ConfigMap;
import com.quiptmc.core.config.ConfigTemplate;
import com.quiptmc.core.config.ConfigValue;

import java.io.File;

@ConfigTemplate(name = "donations")
public class DonationConfig extends Config {

    @ConfigValue
    public String api_endpoint = "https://www.extra-life.org/api/";

    @ConfigValue
    public int seconds_per_check = 15;

    @ConfigValue
    public int team_id = 69005;

    @ConfigValue
    public int donations = 0;

    @ConfigValue
    public double total = 0.0;

    @ConfigValue
    public ConfigMap<Donation> processed = new ConfigMap<>();

    @ConfigValue
    public String etag = "null";


    public DonationConfig(File file, String name, ConfigTemplate.Extension extension, QuiptIntegration integration) {
        super(file, name, extension, integration);

        System.out.println("DonationConfig initialized with teamId: " + team_id);
    }


    public void process(Donation donation) {
        System.out.println(3);
        donation.process();
        System.out.println(13);
        processed.put(donation);
        System.out.println(14);
        total += donation.amount;
    }
}
