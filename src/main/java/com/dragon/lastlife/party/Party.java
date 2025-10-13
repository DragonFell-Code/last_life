package com.dragon.lastlife.party;

import com.dragon.lastlife.players.Participant;
import com.quiptmc.core.config.ConfigMap;
import com.quiptmc.core.config.ConfigObject;
import org.json.JSONArray;
import org.json.JSONObject;

public class Party extends ConfigObject {


    ConfigMap<Participant> members = new ConfigMap<>();

    public Party() {

    }

    public Party(JSONObject json) {
        this.fromJson(json);
    }

}
