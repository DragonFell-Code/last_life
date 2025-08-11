package com.dragon.lastlife.config;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.config.factories.Factories;
import com.dragon.lastlife.utils.chat.MessageUtils;
import com.quiptmc.core.config.ConfigManager;

public class Configs {

    public final DefaultConfig DEFAULT_CONFIG;
    public final ParticipantConfig PARTICIPANT_CONFIG;
    public final MessageUtils MESSAGE_CONFIG;

    public final Factories FACTORIES;

    public Configs(Initializer initializer) {
        FACTORIES = new Factories();

        DEFAULT_CONFIG = ConfigManager.registerConfig(initializer.integration(), DefaultConfig.class);
        PARTICIPANT_CONFIG = ConfigManager.registerConfig(initializer.integration(), ParticipantConfig.class);
        MESSAGE_CONFIG = new MessageUtils();
    }
}
