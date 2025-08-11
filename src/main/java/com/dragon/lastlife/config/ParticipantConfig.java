package com.dragon.lastlife.config;

import com.dragon.lastlife.players.Participant;
import com.quiptmc.core.QuiptIntegration;
import com.quiptmc.core.config.Config;
import com.quiptmc.core.config.ConfigMap;
import com.quiptmc.core.config.ConfigTemplate;
import com.quiptmc.core.config.ConfigValue;

import java.io.File;
import java.util.UUID;

@ConfigTemplate(name = "participants")
public class ParticipantConfig extends Config {

    @ConfigValue
    public ConfigMap<Participant> cache = new ConfigMap<>();

    public ParticipantConfig(File file, String name, ConfigTemplate.Extension extension, QuiptIntegration integration) {
        super(file, name, extension, integration);
    }

    public Participant get(UUID uuid) {
        if (cache.contains(uuid.toString())) {
            return cache.get(uuid.toString());
        }
        Participant participant = new Participant(uuid.toString(), "none", 3);
        cache.put(participant);
        save();
        return participant;
    }

}
