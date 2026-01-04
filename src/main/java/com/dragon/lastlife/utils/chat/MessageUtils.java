package com.dragon.lastlife.utils.chat;

import com.dragon.lastlife.utils.Utils;
import com.quiptmc.core.config.ConfigManager;
import com.quiptmc.core.config.files.MessagesConfig;
import com.quiptmc.core.data.registries.Registries;
import com.quiptmc.core.data.registries.Registry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

        register("cmd.party.usage", text("Usage: /party <create|join|leave|remove>"));
        register("cmd.party.create.usage", text("Usage: /party create <partyName>"));
        register("cmd.party.join.usage", text("Usage: /party join <partyName> [player(s)]"));
        register("cmd.party.leave.usage", text("Usage: /party leave"));
        register("cmd.party.remove.usage", text("Usage: /party remove <partyName>"));

        register("cmd.donations.usage", text("Usage: /donations <incentives|link>"));
        register("cmd.donations.incentives.usage", text("Usage: /donations incentives <life|boogey|shulker_loot|bundle_loot> <incentiveName>"));
        register("cmd.donations.link.usage", text("Usage: /donations link <participantName>"));

        register("cmd.boogey.set.usage", text("Usage: /boogey set <player> [true|false]"));

        register("cmd.settings.usage", text("Usage: /settings <view|set> <setting> <value>"));

        register("lastlife.party.create", text("Party \"", NamedTextColor.YELLOW).append(text("[0]", NamedTextColor.GREEN).append(text("\" has been created.", NamedTextColor.YELLOW))));
        register("lastlife.party.remove", text("Party \"", NamedTextColor.YELLOW).append(text("[0]", NamedTextColor.GREEN).append(text("\" has been removed.", NamedTextColor.YELLOW))));
        register("lastlife.party.join", text("You have joined the party \"", NamedTextColor.YELLOW).append(text("[0]", NamedTextColor.GREEN).append(text("\".", NamedTextColor.YELLOW))));
        register("lastlife.party.leave", text("You have left the party \"", NamedTextColor.YELLOW).append(text("[0]", NamedTextColor.GREEN).append(text("\".", NamedTextColor.YELLOW))));
        register("lastlife.party.join.other", text("[0] has joined the party \"", NamedTextColor.YELLOW).append(text("[1]", NamedTextColor.GREEN).append(text("\".", NamedTextColor.YELLOW))));
        register("lastlife.party.leave.other", text("[0] has left the party \"", NamedTextColor.YELLOW).append(text("[1]", NamedTextColor.GREEN).append(text("\".", NamedTextColor.YELLOW))));

        register("lastlife.settings.set", text("[0] has been set to [1]", NamedTextColor.YELLOW));
        register("lastlife.settings.view", text("[0]: [1]", NamedTextColor.YELLOW));


        register("lastlife.death.elimination", text("[0]", NamedTextColor.RED).append(text(" has been eliminated!", NamedTextColor.WHITE)));
        register("cmd.boogey.set", text("[0]", NamedTextColor.GREEN).append(text(" boogeyman state set to ", NamedTextColor.YELLOW).append(text("[1]", NamedTextColor.GOLD))));
        register("lastlife.boogey.roll.multiple", text("[0] boogeymen are about to be selected.", NamedTextColor.RED));
        register("lastlife.boogey.roll", text("[0] boogeyman is about to be selected.", NamedTextColor.RED));
        register("lastlife.boogey.cured", text("You've been cured! You are no longer a boogeyman.", NamedTextColor.GREEN));
        register("lastlife.boogey.set", text("You are now a boogeyman!", NamedTextColor.RED));

        register("lastlife.dungeon.chests_reset", text("The Labyrinth chests have been refilled !", NamedTextColor.GREEN));

        register("lastlife.cmd.life.view", text("[0]'s lives: [1]", NamedTextColor.YELLOW));
        register("lastlife.cmd.life.set", text("Set [0]'s lives to [1].", NamedTextColor.GREEN));

        register("lastlife.cmd.loot_level.get", text("Current loot level is [0][1]", NamedTextColor.WHITE));
        register("lastlife.cmd.loot_level.set", text("Loot level has been overridden successfully. (Current Level: [0])", NamedTextColor.GREEN));
        register("lastlife.cmd.loot_level.reset", text("Loot level has been reset and is now dependent on donations. (Current Level: [0])", NamedTextColor.WHITE));

        register("lastlife.cmd.dungeon.generating", text("The Labyrinth is generating. This might take a while depending on the size of the Labyrinth", NamedTextColor.GRAY));
        register("lastlife.cmd.dungeon.auto_size", text("Labyrinth size: [0]", NamedTextColor.GRAY));
        register("lastlife.cmd.dungeon.generated", text("The Labyrinth was generated successfully. Check it out with /labyrinth tp. If it looks good, open it for players with /labyrinth open", NamedTextColor.GREEN));
        register("lastlife.cmd.dungeon.generate_fail", text("The Labyrinth failed to generate: [0]", NamedTextColor.RED));
        register("lastlife.cmd.dungeon.open", text("The Labyrinth is now OPEN", NamedTextColor.GREEN));
        register("lastlife.cmd.dungeon.closed", text("The Labyrinth is now CLOSED", NamedTextColor.RED));
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
        return text.replaceText(builder -> {
            for (String key : keys) {
                builder.match("\\$\\{" + key + "\\}").replacement(get(key));
            }

        });
    }
}
