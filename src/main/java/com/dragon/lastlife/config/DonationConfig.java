package com.dragon.lastlife.config;

import com.dragon.lastlife.donations.Donation;
import com.quiptmc.core.QuiptIntegration;
import com.quiptmc.core.config.Config;
import com.quiptmc.core.config.ConfigMap;
import com.quiptmc.core.config.ConfigTemplate;
import com.quiptmc.core.config.ConfigValue;

import java.io.File;
import java.math.BigDecimal;

@ConfigTemplate(name = "donations", ext = ConfigTemplate.Extension.JSON)
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
    public BigDecimal total = BigDecimal.valueOf(0.0);

    @ConfigValue
    public ConfigMap<Donation> processed = new ConfigMap<>();

    @ConfigValue
    public String etag = "null";


    public DonationConfig(File file, String name, ConfigTemplate.Extension extension, QuiptIntegration integration) {
        super(file, name, extension, integration);
    }


    public Donation.ProcessResult<?> process(Donation donation) {
        Donation.ProcessResult<?> result = donation.process();
        processed.put(donation);
        total = BigDecimal.valueOf(total.doubleValue() + donation.amount);
        return result;
    }

    public boolean processed(Donation donation) {
        return processed.contains(donation.id);
    }
}
