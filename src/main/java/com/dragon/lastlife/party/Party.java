package com.dragon.lastlife.party;

import com.dragon.lastlife.players.Participant;
import com.dragon.lastlife.utils.Utils;
import com.quiptmc.core.config.ConfigMap;
import com.quiptmc.core.config.ConfigObject;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

public class Party extends ConfigObject {


    public ConfigMap<Participant> members = new ConfigMap<>();

    public Party() {

    }

    public Party(JSONObject json) {
        this.fromJson(json);
    }

    public void join(Participant participant) {
        members.put(participant);
        Utils.configs().PARTY_CONFIG().save();
    }

    public void leave(Participant participant) {
        members.remove(participant);
        Utils.configs().PARTY_CONFIG().save();
    }
}
