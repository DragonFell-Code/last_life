package com.dragon.lastlife.config.factories;

import com.dragon.lastlife.config.object.ConfigLocation;
import com.quiptmc.core.config.ConfigObject;
import org.json.JSONObject;

public class ConfigLocationFactory implements ConfigObject.Factory<ConfigLocation> {
    @Override
    public String getClassName() {
        return ConfigLocation.class.getName();
    }

    @Override
    public ConfigLocation createFromJson(JSONObject json) {
        return new ConfigLocation(json);
    }
}
