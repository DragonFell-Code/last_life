package com.dragon.lastlife.config;

import com.dragon.lastlife.party.Party;
import com.quiptmc.core.QuiptIntegration;
import com.quiptmc.core.config.Config;
import com.quiptmc.core.config.ConfigTemplate;
import com.quiptmc.core.data.registries.Registries;
import com.quiptmc.core.data.registries.Registry;
import com.quiptmc.core.data.registries.RegistryKey;
import org.json.JSONArray;

import java.awt.*;
import java.io.File;
import java.util.Optional;

@ConfigTemplate(name = "parties")
public class PartyConfig extends Config {

    public JSONArray parties = new JSONArray();

    private Registry<Party> partyCache = Registries.register("party_cache", ()->new Party("admin", "Admin Team", new Color(0xFF0000).getRGB()));

    public PartyConfig(File file, String name, ConfigTemplate.Extension extension, QuiptIntegration integration) {
        super(file, name, extension, integration);
    }

    public Registry<Party> cache(){
        return partyCache;
    }

    public Party get(String id){
        Optional<Party> cachedParty = partyCache.get(id);
        if(cachedParty.isPresent()) return cachedParty.get();
        for (int i = 0; i < parties.length(); i++) {
            if (parties.getJSONObject(i).getString("id").equals(id)) {
                Party party = new Party();
                party.fromJson(parties.getJSONObject(i));
                partyCache.register(id, party);
                return party;
            }
        }
        return null;
    }

    public void add(Party party) {
        parties.put(party.json());
    }


}
