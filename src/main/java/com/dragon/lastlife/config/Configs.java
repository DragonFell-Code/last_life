package com.dragon.lastlife.config;

import com.dragon.lastlife.Initializer;
import com.quiptmc.core.config.ConfigManager;

public class Configs {

    public final DefaultConfig DEFAULT_CONFIG;
    public final PartyConfig PARTY_CONFIG;

    public Configs(Initializer initializer){
        DEFAULT_CONFIG = ConfigManager.registerConfig(initializer.integration(), DefaultConfig.class);
        PARTY_CONFIG = ConfigManager.registerConfig(initializer.integration(), PartyConfig.class);
    }
}
