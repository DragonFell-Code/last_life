package com.dragon.lastlife.config;


import com.dragon.lastlife.party.Party;
import com.quiptmc.core.QuiptIntegration;
import com.quiptmc.core.config.Config;
import com.quiptmc.core.config.ConfigTemplate;
import com.quiptmc.core.config.ConfigValue;
import com.quiptmc.core.data.JsonSerializable;
import com.quiptmc.core.data.registries.Registries;
import com.quiptmc.core.data.registries.Registry;

import java.awt.*;
import java.io.File;
import java.util.Optional;

@ConfigTemplate(name = "config")
public class DefaultConfig extends Config {


    public DefaultConfig(File file, String name, ConfigTemplate.Extension extension, QuiptIntegration integration) {
        super(file, name, extension, integration);
    }

}
