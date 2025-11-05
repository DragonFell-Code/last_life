package com.dragon.lastlife.config.factories;

import com.quiptmc.core.config.ConfigObject;
import com.quiptmc.core.config.objects.ConfigString;
import org.json.JSONObject;


public class ConfigStringFactory implements ConfigObject.Factory<ConfigString> {

    @Override
    public String getClassName() {
        return ConfigString.class.getName();
    }

    @Override
    public ConfigString createFromJson(JSONObject json) {
        return new ConfigString(json);
    }
}
