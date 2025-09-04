package com.dragon.lastlife.config;

import com.dragon.lastlife.utils.Utils;
import com.quiptmc.core.QuiptIntegration;
import com.quiptmc.core.config.Config;
import com.quiptmc.core.config.ConfigMap;
import com.quiptmc.core.config.ConfigTemplate;
import com.quiptmc.core.config.ConfigValue;
import com.quiptmc.core.discord.Webhook;
import com.quiptmc.core.discord.WebhookManager;
import net.kyori.adventure.text.Component;

import java.io.File;

import static net.kyori.adventure.text.Component.text;

@ConfigTemplate(name = "webhooks", ext = ConfigTemplate.Extension.JSON)
public class WebhooksConfig extends Config {

    @ConfigValue
    public ConfigMap<Webhook> webhooks = new ConfigMap<>();

    private boolean initialized = false;


    /**
     * Creates a new config file
     *
     * @param file        The file to save to
     * @param name        The name of the config
     * @param extension   The extension of the config
     * @param integration The plugin that owns this config
     */
    public WebhooksConfig(File file, String name, ConfigTemplate.Extension extension, QuiptIntegration integration) {
        super(file, name, extension, integration);
    }

    public void initialize(){
        if(initialized)
            integration().logger().warn("[WebhooksConfig] Already initialized... Initializing again.");
        initialized = true;
        if(webhooks.size() == 0){
            integration().logger().warn("[WebhooksConfig] No webhooks registered, skipping...");
            return;
        }

        for(Webhook webhook : webhooks.values()){
            integration().logger().log("[WebhooksConfig] Registering webhook: " + webhook.name());
            WebhookManager.add(webhook);
        }
    }

    public boolean initialized(){
        return initialized;
    }
}
