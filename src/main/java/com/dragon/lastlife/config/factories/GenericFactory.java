package com.dragon.lastlife.config.factories;

import com.quiptmc.core.config.ConfigObject;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;

public record GenericFactory<T extends ConfigObject>(Class<T> type) implements ConfigObject.Factory<T> {


    @Override
    public String getClassName() {
        return type.getName();
    }

    @Override
    public T createFromJson(JSONObject json) {
        try {
            T instance = type.getConstructor().newInstance();
            instance.fromJson(json);
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(getClassName() + " does not support generic factories. You must create one manually.", e);
        }
    }
}
