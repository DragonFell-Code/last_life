package com.dragon.lastlife.utils.net;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.quiptmc.core.annotations.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MessageChannelHandler {

    private final JavaPlugin plugin;
    private final Map<NamespacedKey, MessageChannel> channels = new HashMap<>();


    public MessageChannelHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public MessageChannel register(MessageChannel.Type type, String name, @Nullable PluginMessageListener incomingListener) {
        MessageChannel channel = new MessageChannel(type, name);
        NamespacedKey key = key(name);
        if (channels.containsKey(key)) {
            plugin.getLogger().warning("Channel " + key.asString() + " is already registered.");
            return channels.get(key);
        }
        channels.put(key, channel);
        if (type == MessageChannel.Type.INCOMING) {
            Bukkit.getServer().getMessenger().registerIncomingPluginChannel(plugin, key.asString(), incomingListener);
        } else if (type == MessageChannel.Type.OUTGOING) {
            Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(plugin, key.asString());
        } else {
            plugin.getLogger().warning("Unknown channel type: " + type);
            return null;
        }

        plugin.getLogger().info("Registered " + type.name().toLowerCase() + " channel: " + key.asString());
        return channel;
    }

    public MessageChannel get(String channel) {
        return channels.getOrDefault(key(channel), null);
    }

    public NamespacedKey key(String channel) {
        return new NamespacedKey(plugin, channel);
    }


    public <T> void send(String channel, Player player, byte flag, Object... args) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        for (Object arg : args) {
            switch (arg) {
                case String str -> {
                    var(out, str.length());
                    out.write(str.getBytes(StandardCharsets.UTF_8));
                }
                case Integer integer -> out.writeInt(integer);
                case Long lng -> out.writeLong(lng);
                case Byte b -> out.writeByte(b);
                case Boolean bool -> out.writeBoolean(bool);
                default -> plugin.getLogger().warning("Unsupported argument type: " + arg.getClass().getName());
            }
        }

        //write position data
        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();
        // Pack x, y, z into a single long as Minecraft does
        long packedPos = ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (y & 0xFFF);
        out.writeLong(packedPos);

        // Write the flag byte
        out.writeByte(flag);
        player.sendPluginMessage(plugin, key(channel).asString(), out.toByteArray());
    }

    private void var(ByteArrayDataOutput out, int value) {
        while (true) {
            if ((value & ~0x7F) == 0) {
                out.writeByte(value);
                return;
            }

            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
    }
}
