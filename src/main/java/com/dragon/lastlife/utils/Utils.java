package com.dragon.lastlife.utils;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.config.Configs;
import com.dragon.lastlife.donations.DonationFlutter;
import com.dragon.lastlife.utils.chat.placeholder.PlaceholderUtils;
import com.dragon.lastlife.utils.net.MessageChannel;
import com.dragon.lastlife.utils.net.MessageChannelHandler;
import com.dragon.lastlife.utils.net.listener.ClientToServerListener;
import com.quiptmc.core.config.ConfigManager;
import com.quiptmc.core.discord.WebhookManager;
import com.quiptmc.core.heartbeat.Flutter;
import com.quiptmc.core.heartbeat.HeartbeatUtils;

import java.util.concurrent.TimeUnit;

public class Utils {

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
//    private static

    public static void init(Initializer init) {
        initializer = init;
        configs = new Configs(init);
        PlaceholderUtils.registerPlaceholders();

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
        // https://discord.com/api/webhooks/1412263980898058320/-vIqhs5Wmc92ycrbO-ZMVH-Bo75tct7BApjMiT8x5y3wPRHKmtmY-DEGYH_ZWyx1wLX8
    }

    private static void setupHeartbeat() {
        System.out.println("Setting up heartbeats...");

        try {
            HeartbeatUtils.init(initializer().integration());

            HeartbeatUtils.heartbeat(initializer().integration()).flutter(SAVE_FLUTTER);
            HeartbeatUtils.heartbeat(initializer().integration()).flutter(new DonationFlutter(configs().DONATION_CONFIG()));
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
}
