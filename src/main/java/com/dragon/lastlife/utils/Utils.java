package com.dragon.lastlife.utils;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.boogey.BoogeyFlutter;
import com.dragon.lastlife.config.Configs;
import com.dragon.lastlife.donations.DonationFlutter;
import com.dragon.lastlife.loot.LootManager;
import com.dragon.lastlife.utils.chat.placeholder.PlaceholderUtils;
import com.dragon.lastlife.utils.net.MessageChannel;
import com.dragon.lastlife.utils.net.MessageChannelHandler;
import com.dragon.lastlife.utils.net.listener.ClientToServerListener;
import com.quiptmc.core.annotations.Nullable;
import com.quiptmc.core.config.ConfigManager;
import com.quiptmc.core.discord.WebhookManager;
import com.quiptmc.core.discord.embed.Embed;
import com.quiptmc.core.heartbeat.Flutter;
import com.quiptmc.core.heartbeat.HeartbeatUtils;
import com.quiptmc.core.heartbeat.runnable.Heartbeat;

import java.awt.*;
import java.util.concurrent.TimeUnit;

import static net.kyori.adventure.text.Component.text;

public class Utils {


    private static DonationFlutter DONATION_FLUTTER;
    private static final Flutter SAVE_FLUTTER = new Flutter() {
        private long lastHeartbeat = 0;
        private long delay = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES);

        @Override
        public boolean run() {

            long now = System.currentTimeMillis();
            if (lastHeartbeat + delay <= now) {
                lastHeartbeat = now;
                ConfigManager.saveAll();
            }
            return true;
        }
    };
    private static Initializer initializer;
    private static Configs configs;
    private static MessageChannelHandler channelMessageHandler;

    public static void init(Initializer init) {
        initializer = init;
        init.integration().log("Utils", "Initializing...");
        configs = new Configs(init);
        init.integration().log("Utils", "Configs initialized");
        PlaceholderUtils.registerPlaceholders();
        init.integration().log("Utils", "Registered placeholders");

        channelMessageHandler = new MessageChannelHandler(init);
        MessageChannel stc = channelMessageHandler().register(MessageChannel.Type.OUTGOING, "stc", null);
        if (stc == null) {
            init.integration().log("Utils", "Failed to register outgoing channel 'stc'.");
        } else {
            init.integration().log("Utils", "Registered outgoing channel: " + stc.name);
        }

        MessageChannel cts = channelMessageHandler().register(MessageChannel.Type.INCOMING, "cts", new ClientToServerListener());
        if (cts == null) {
            init.integration().log("Utils", "Failed to register outgoing channel 'cts'.");
        } else {
            init.integration().log("Utils", "Registered outgoing channel: " + cts.name);
        }
        setupHeartbeat();
        init.integration().log("Utils", "Heartbeats set up");
    }

    private static void setupHeartbeat() {
        initializer().getComponentLogger().info(text("Setting up heartbeats..."));
        try {
            Heartbeat heartbeat = HeartbeatUtils.init(initializer().integration());

            heartbeat.flutter(SAVE_FLUTTER);
            DONATION_FLUTTER = new DonationFlutter(configs().DONATION_CONFIG());
            heartbeat.flutter(DONATION_FLUTTER);
            heartbeat.flutter(new BoogeyFlutter());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Initializer initializer() {
        return initializer;
    }

    public static Configs configs() {
        return configs;
    }

    public static MessageChannelHandler channelMessageHandler() {
        return channelMessageHandler;
    }

    public static void genericWebhook(String channelName, Color color, String title, @Nullable String image, @Nullable String description, @Nullable Embed.Field... fields) {
        if(WebhookManager.get(channelName) == null) return;
        Embed.Builder builder = Embed.builder()
                .title(title)
                .description(description)
                .color(color.getRGB() & 0xFFFFFF);
        if (image != null) builder.thumbnail(image);
        if (fields != null) {
            for (Embed.Field fieldItem : fields) {
                if (fieldItem != null) {
                    builder.field(fieldItem.name, fieldItem.value, fieldItem.inline);
                }
            }

        }
        WebhookManager.send(channelName, builder.build());
    }

    public static DonationFlutter donations() {
        return DONATION_FLUTTER;
    }
}
