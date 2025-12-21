package com.dragon.lastlife.config;

import com.dragon.lastlife.config.object.ConfigLocation;
import com.quiptmc.core.QuiptIntegration;
import com.quiptmc.core.config.Config;
import com.quiptmc.core.config.ConfigMap;
import com.quiptmc.core.config.ConfigTemplate;
import com.quiptmc.core.config.ConfigValue;

import java.io.File;

@ConfigTemplate(name = "pois", ext = ConfigTemplate.Extension.QPT)
public class PoiConfig extends Config {



    @ConfigValue
    public ConfigMap<ConfigLocation> pois = new ConfigMap<>();


    /**
     * Creates a new config file
     *
     * @param file        The file to save to
     * @param name        The name of the config
     * @param extension   The extension of the config
     * @param integration The plugin that owns this config
     */
    public PoiConfig(File file, String name, ConfigTemplate.Extension extension, QuiptIntegration integration) {
        super(file, name, extension, integration);
    }

    public ConfigLocation random() {
        return pois.values().stream().skip((int) (Math.random() * pois.size())).findFirst().orElse(null);
    }
}
