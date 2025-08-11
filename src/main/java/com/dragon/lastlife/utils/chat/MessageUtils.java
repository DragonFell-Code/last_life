package com.dragon.lastlife.utils.chat;

import com.dragon.lastlife.utils.Utils;
import com.quiptmc.core.config.ConfigManager;
import com.quiptmc.core.config.files.MessagesConfig;
import com.quiptmc.core.data.registries.Registries;
import com.quiptmc.core.data.registries.Registry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;


public class MessageUtils {

    private final Registry<Component> registry = Registries.register("messages", () -> null);

    private final MessagesConfig config;

    public MessageUtils() {
        config = ConfigManager.registerConfig(Utils.initializer().integration(), MessagesConfig.class);
        createDefaultMessages();
        for (String key : config.messages.keySet()) {
            if (registry.get(key).isEmpty()) {
                registry.register(key, deserialize(config.messages.getString(key)));
            }
        }
        config.save();
    }

    public void register(String key, String serializedComponent) {
        if (!config.messages.has(key)) {
            registry.register(key, deserialize(serializedComponent));
            config.messages.put(key, serializedComponent);
        }
    }

    public void register(String key, Component deserializedComponent) {
        register(key, serialize(deserializedComponent));
    }

    private void createDefaultMessages() {
        register("cmd.error.no_perm", "{\"text\":\"Sorry, you don't have the permission to run that command.\",\"color\":\"red\"}");
        register("cmd.error.no_command", "{\"text\":\"Sorry, couldn't find the command \\\"[0]\\\". Please check your spelling and try again.\",\"color\":\"red\"}");
        register("cmd.error.no_console", "{\"text\":\"Sorry, this command can only be run by players.\",\"color\":\"red\"}");
        register("cmd.session.start", "{\"text\":\"Session started\",\"color\":\"green\"}");
        register("cmd.session.end", "{\"text\":\"Session ended\",\"color\":\"green\"}");
        register("cmd.session.reward", "{\"text\":\"You have been rewarded with [0]\",\"color\":\"green\"}");
        register("cmd.session.task", "{\"text\":\"You have been assigned the task [0]\",\"color\":\"green\"}");
    }

    public Component deserialize(JSONObject json) {
        return deserialize(json.toString());
    }

    public Component deserialize(String json) {
        return GsonComponentSerializer.gson().deserialize(json);
    }

    public String plainText(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    public String serialize(Component component) {
        String a = GsonComponentSerializer.gson().serialize(component);
        if (!a.startsWith("{")) a = "{\"text\":" + a + "}";
        return a;
    }

    public Component get(String key, Object... replacements) {
        Component a = get(key);
        for (int i = 0; i != replacements.length; i++) {
            int finalI = i;
            a = a.replaceText(builder -> {
                Component replacement;
                if (replacements[finalI] instanceof Component) replacement = (Component) replacements[finalI];
                else replacement = text(replacements[finalI].toString());
                builder.match("\\[" + finalI + "\\]").replacement(replacement);
            });
        }
        return a;
    }

    private Component get(String key) {
        return registry.getOrDefault(key, translatable(key));
    }

    public Component parse(@NotNull Component text) {
        String content = plainText(text);
        List<String> keys = new ArrayList<>();
        while(content.contains("${") && content.contains("}")) {
            int start = content.indexOf("${");
            int end = content.indexOf("}", start);
            String key = content.substring(start + 2, end);
            keys.add(key);
            content = content.replace("${" + key + "}", "");
        }
        Component result = text.replaceText(builder -> {
            for (String key : keys) {
                builder.match("\\$\\{" + key + "\\}").replacement(get(key));
            }

        });
        System.out.println("Content: " + content);
        return result;
    }
}
