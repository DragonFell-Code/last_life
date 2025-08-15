package com.dragon.lastlife.config.factories;

import com.dragon.lastlife.donations.Donation;
import com.dragon.lastlife.players.Participant;
import com.quiptmc.core.config.ConfigObject;
import org.json.JSONObject;

public class DonationFactory implements ConfigObject.Factory<Donation> {

    @Override
    public String getClassName() {
        return Donation.class.getName();
    }

    @Override
    public Donation createFromJson(JSONObject json) {
        return new Donation(json);
    }
}
