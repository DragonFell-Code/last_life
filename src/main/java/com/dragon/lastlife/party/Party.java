package com.dragon.lastlife.party;

import com.dragon.lastlife.config.PartyConfig;
import com.dragon.lastlife.players.Participant;
import com.dragon.lastlife.utils.Utils;
import com.quiptmc.core.config.ConfigMap;
import com.quiptmc.core.config.ConfigObject;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Optional;

public class Party extends ConfigObject {


    public ConfigMap<Participant> members = new ConfigMap<>();

    public Party() {

    }

    public Party(JSONObject json) {
        this.fromJson(json);
    }

    public void join(Participant participant) {
        PartyConfig config = Utils.configs().PARTY_CONFIG();
        Optional<Party> previousParty = config.get(participant);
        previousParty.ifPresent(party -> party.leave(participant));

        members.put(participant);
        Utils.configs().PARTY_CONFIG().save();
        Player player = participant.player().getPlayer();
        if(player != null)
            player.sendMessage(Utils.configs().MESSAGE_CONFIG.get("lastlife.party.join", this.id));
    }

    public void leave(Participant participant) {
        members.remove(participant);
        Utils.configs().PARTY_CONFIG().save();
        Player player = participant.player().getPlayer();
        if(player != null)
            player.sendMessage(Utils.configs().MESSAGE_CONFIG.get("lastlife.party.leave", this.id));

    }
}
