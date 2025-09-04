package com.dragon.lastlife.config.factories;

import com.dragon.lastlife.players.Participant;
import com.quiptmc.core.config.ConfigObject;
import com.quiptmc.core.discord.Webhook;
import org.json.JSONObject;

public class WebhookFactory implements ConfigObject.Factory<Webhook> {

    @Override
    public String getClassName() {
        return Webhook.class.getName();
    }

    @Override
    public Webhook createFromJson(JSONObject json) {
        Webhook webhook = new Webhook();
        webhook.fromJson(json);
        return webhook;
    }
}
