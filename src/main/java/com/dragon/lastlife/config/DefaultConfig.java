package com.dragon.lastlife.config;


import com.quiptmc.core.QuiptIntegration;
import com.quiptmc.core.config.Config;
import com.quiptmc.core.config.ConfigTemplate;

import java.io.File;

@ConfigTemplate(name = "config")
public class DefaultConfig extends Config {


    public DefaultConfig(File file, String name, ConfigTemplate.Extension extension, QuiptIntegration integration) {
        super(file, name, extension, integration);
    }

}
