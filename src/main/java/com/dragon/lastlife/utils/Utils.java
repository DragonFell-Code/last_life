package com.dragon.lastlife.utils;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.config.Configs;
import com.dragon.lastlife.utils.chat.placeholder.PlaceholderUtils;
import com.dragon.lastlife.utils.net.MessageChannel;
import com.dragon.lastlife.utils.net.MessageChannelHandler;
import com.quiptmc.core.config.ConfigManager;
import com.quiptmc.core.heartbeat.Flutter;
import com.quiptmc.core.heartbeat.HeartbeatUtils;

import java.util.concurrent.TimeUnit;

public class Utils {

    private static Initializer initializer;
    private static Configs configs;

    private static MessageChannelHandler channelMessageHandler;
//    private static


    public static void init(Initializer init) {
        initializer = init;
        configs = new Configs(init);
        PlaceholderUtils.registerPlaceholders();

        channelMessageHandler = new MessageChannelHandler(init);
        MessageChannel channel = channelMessageHandler().register(MessageChannel.Type.OUTGOING, "data", null);
        if(channel == null) {
            init.integration().log("Utils", "Failed to register outgoing channel 'data'.");
        } else {
            init.integration().log("Utils", "Registered outgoing channel: " + channel.name);
        }

        HeartbeatUtils.init(init.integration());
        HeartbeatUtils.heartbeat(init.integration()).flutter(new Flutter() {
            private long lastHeartbeat = 0;
            private long delay = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES);

            @Override
            public boolean run() {
                long now = System.currentTimeMillis();
                if(lastHeartbeat + delay <= now) {
                    lastHeartbeat = now;
                    ConfigManager.saveAll();
                }
                return true;
            }
        });

    }

    public static Initializer initializer(){
        return initializer;
    }

    public static Configs configs() {
        return configs;
    }

    public static MessageChannelHandler channelMessageHandler() {
        return channelMessageHandler;
    }
}
