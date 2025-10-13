package com.dragon.lastlife.config;

import com.dragon.lastlife.party.Party;
import com.dragon.lastlife.players.Participant;
import com.quiptmc.core.QuiptIntegration;
import com.quiptmc.core.config.Config;
import com.quiptmc.core.config.ConfigMap;
import com.quiptmc.core.config.ConfigTemplate;
import com.quiptmc.core.config.ConfigValue;
import org.json.JSONObject;

import java.io.File;
import java.util.Optional;

@ConfigTemplate(name = "party", ext = ConfigTemplate.Extension.JSON)
public class PartyConfig extends Config {

    @ConfigValue
    public ConfigMap<Party> parties = new ConfigMap<>();

    /**
     * Creates a new config file
     *
     * @param file        The file to save to
     * @param name        The name of the config
     * @param extension   The extension of the config
     * @param integration The plugin that owns this config
     */
    public PartyConfig(File file, String name, ConfigTemplate.Extension extension, QuiptIntegration integration) {
        super(file, name, extension, integration);
    }

    public Party create(String name){
        if(parties.contains(name)) return get(name);
        JSONObject partyData = new JSONObject();
        partyData.put("id", name);
        Party party = new Party(partyData);
        parties.put(party);
        save();
        return party;
    }

    public Party get(String name){
        if(!parties.contains(name)) return null;
        return parties.get(name);
    }

    public Optional<Party> get(Participant participant){
        for(Party party : parties.values()){
            if(party.members.contains(participant.id)){
                return Optional.of(party);
            }
        }
        return Optional.empty();
    }
}
