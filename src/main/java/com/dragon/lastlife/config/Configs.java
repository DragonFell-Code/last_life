package com.dragon.lastlife.config;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.utils.chat.MessageUtils;
import com.quiptmc.core.config.ConfigManager;

public class Configs {

    public final DefaultConfig DEFAULT_CONFIG;
    public final ParticipantConfig PARTICIPANT_CONFIG;
    public final MessageUtils MESSAGE_CONFIG;

    public Configs(Initializer initializer) {
        DEFAULT_CONFIG = ConfigManager.registerConfig(initializer.integration(), DefaultConfig.class);
        PARTICIPANT_CONFIG = ConfigManager.registerConfig(initializer.integration(), ParticipantConfig.class);
        MESSAGE_CONFIG = new MessageUtils();
    }
}
