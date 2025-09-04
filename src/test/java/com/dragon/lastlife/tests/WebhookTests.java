package com.dragon.lastlife.tests;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.config.WebhooksConfig;
import com.dragon.lastlife.config.factories.WebhookFactory;
import com.dragon.lastlife.tests.factory.FakeIntegration;
import com.quiptmc.core.config.ConfigManager;
import com.quiptmc.core.discord.WebhookManager;
import com.quiptmc.core.discord.embed.Embed;
import org.junit.jupiter.api.Test;

public class WebhookTests {

    @Test
    public void testBoogeyWebhook() {
        FakeIntegration integration = new FakeIntegration();
        ConfigManager.registerFactory(new WebhookFactory());
        WebhooksConfig config = ConfigManager.registerConfig(integration, WebhooksConfig.class);
        config.initialize();
        Embed embed = new Embed()
                .title("Boogeyman Selected!")
                .thumbnail("https://mc-heads.net/combo/60191757-427b-421e-bee0-399465d7e852")
                .description("QuickScythe has been selected as a boogeyman!")
                .color(0xE56144);
        WebhookManager.send("boogeymen", embed);

    }
}
