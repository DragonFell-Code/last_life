package com.dragon.lastlife.config;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.config.factories.Factories;
import com.dragon.lastlife.utils.chat.MessageUtils;
import com.quiptmc.core.config.ConfigManager;

public class Configs {

    public final MessageUtils MESSAGE_CONFIG;
    public final Factories FACTORIES;

    private final Initializer initializer;

    public Configs(Initializer initializer) {
        this.initializer = initializer;
        FACTORIES = new Factories();
        MESSAGE_CONFIG = new MessageUtils();

        ConfigManager.registerConfig(initializer.integration(), DefaultConfig.class);
        ConfigManager.registerConfig(initializer.integration(), ParticipantConfig.class);
        ConfigManager.registerConfig(initializer.integration(), DonationConfig.class);
    }

    public DefaultConfig DEFAULT_CONFIG() {
        return ConfigManager.getConfig(initializer.integration(), DefaultConfig.class);
    }

    public ParticipantConfig PARTICIPANT_CONFIG() {
        return ConfigManager.getConfig(initializer.integration(), ParticipantConfig.class);
    }

    public DonationConfig DONATION_CONFIG() {
        return ConfigManager.getConfig(initializer.integration(), DonationConfig.class);
    }
}
