package com.dragon.lastlife.utils.net.listener;

import com.dragon.lastlife.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ClientToServerListener implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        try {
            String jsonPart = extractJson(message);
            if (jsonPart.startsWith("$")) {
                jsonPart = jsonPart.substring(1);
            }
            Utils.configs().PARTICIPANT_CONFIG().get(player.getUniqueId()).modded = true;
            Utils.configs().PARTICIPANT_CONFIG().save();

        } catch (Exception e) {
            System.err.println("Error decoding message from player " + player.getName() + ": " + e.getMessage());
        }
    }

    private static @NotNull String extractJson(byte @NotNull [] message) {
        int jsonEndIndex = -1;
        for (int i = 0; i < message.length; i++) {
            if (message[i] == '}') {
                jsonEndIndex = i + 1; // Include the closing brace
                break;
            }
        }

        if (jsonEndIndex == -1) {
            throw new IllegalArgumentException("Could not find end of JSON data");
        }

        // Extract and parse the JSON part
        return new String(message, 0, jsonEndIndex, StandardCharsets.UTF_8);
    }

    private static @NotNull DataInputStream getDataInputStream(byte @NotNull [] message, int jsonEndIndex) throws IOException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(message, jsonEndIndex, message.length - jsonEndIndex);
        DataInputStream dataStream = new DataInputStream(byteStream);

        // Skip any potential whitespace or delimiters
        int available = dataStream.available();
        while (available > 0 && (dataStream.read() <= 32)) {
            available = dataStream.available();
        }

        // We read one byte too many in the loop, so backtrack
        if (available > 0) {
            byteStream = new ByteArrayInputStream(message, jsonEndIndex + (message.length - jsonEndIndex - available), available);
            dataStream = new DataInputStream(byteStream);
        }
        return dataStream;
    }

    private void processClientData(Player player, String stringValue, int intValue, BlockPos blockPos, byte flag) {
        // Handle different flag values here
        switch (flag) {
            case 0:
                // Handle case 0
                break;
            case 1:
                // Handle case 1
                break;
            // Add more cases as needed
            default:
                Utils.initializer().integration().logger().warn("Unknown flag value: " + flag);
        }
    }
}
