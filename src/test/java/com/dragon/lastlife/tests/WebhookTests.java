package com.dragon.lastlife.tests;

import com.dragon.lastlife.config.WebhooksConfig;
import com.dragon.lastlife.tests.factory.FakeIntegration;
import com.dragon.lastlife.utils.Utils;
import com.quiptmc.core.config.ConfigManager;
import com.quiptmc.core.discord.embed.Embed;
import org.junit.jupiter.api.Test;

import java.awt.*;

public class WebhookTests {

    @Test
    public void testBoogeyWebhook() {
        FakeIntegration integration = new FakeIntegration();
        ConfigManager.registerFactory(new WebhookFactory());
        WebhooksConfig config = ConfigManager.registerConfig(integration, WebhooksConfig.class);
        config.initialize();

        Utils.genericWebhook("boogeymen", new Color(0x0CE1C0), "Boogeyman Selected!", "https://mc-heads.net/head/60191757-427b-421e-bee0-399465d7e852/left.png","QuickScythe has been selected as a boogeyman!",  new Embed.Field("Source", "Automated Test", false));
//
//        Embed embed = new Embed()
//                .title("Boogeyman Selected!")
//                .thumbnail("https://mc-heads.net/combo/60191757-427b-421e-bee0-399465d7e852")
//                .description("QuickScythe has been selected as a boogeyman!")
//                .addField("Source", "Automated Test", false)
//                .color(0x00FF0F);
//        WebhookManager.send("boogeymen", embed);

    }
}
