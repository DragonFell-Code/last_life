package com.dragon.lastlife.tests;

import com.dragon.lastlife.config.WebhooksConfig;
import com.dragon.lastlife.tests.factory.FakeIntegration;
import com.dragon.lastlife.utils.Utils;
import com.quiptmc.core.config.ConfigManager;
import com.quiptmc.core.config.factories.GenericFactory;
import com.quiptmc.core.discord.Webhook;
import com.quiptmc.core.discord.WebhookManager;
import com.quiptmc.core.discord.embed.Embed;
import org.junit.jupiter.api.Test;

import java.awt.*;

public class WebhookTests {

    @Test
    public void testBoogeyWebhook() {
        FakeIntegration integration = new FakeIntegration();
        ConfigManager.registerFactory(new GenericFactory<>(Webhook.class));
        WebhooksConfig config = ConfigManager.registerConfig(integration, WebhooksConfig.class);
        config.initialize();
        WebhookManager.add("boogeymen", "1413082434421133323", "U_veDVfOR7_5KQlPFTBG_gsUu0z9_J9pai1oGWsZdWYL17hdZ9bLKRvVPFjuaj7w4oDR");


        Embed embed = new Embed.Builder()
                .title("Boogeyman Selected!")
                .thumbnail("https://mc-heads.net/combo/60191757-427b-421e-bee0-399465d7e852")
                .description("QuickScythe has been selected as a boogeyman!")
                .field("Source", "Automated Test", false)
                .color(new Color(0x00FF0F))
                .build();
        WebhookManager.send("boogeymen", embed);

    }
}
