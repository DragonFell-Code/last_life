package com.dragon.lastlife.config;

import com.dragon.lastlife.donations.Donation;
import com.quiptmc.core.QuiptIntegration;
import com.quiptmc.core.config.Config;
import com.quiptmc.core.config.ConfigMap;
import com.quiptmc.core.config.ConfigTemplate;
import com.quiptmc.core.config.ConfigValue;
import com.quiptmc.core.heartbeat.Flutter;
import com.quiptmc.core.utils.NetworkUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

@ConfigTemplate(name = "donations")
public class DonationConfig extends Config {


    @ConfigValue
    public int teamId = 69005;
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

        System.out.println("DonationConfig initialized with teamId: " + teamId);
    }


}
