package com.dragon.lastlife.commands.executor;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.commands.CommandExecutor;
import com.dragon.lastlife.commands.executor.argument.ConfigFileArgument;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.quiptmc.core.config.Config;
import com.quiptmc.core.config.ConfigManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.format.NamedTextColor;
import org.json.JSONObject;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;
import static net.kyori.adventure.text.Component.text;

public class ConfigCommand extends CommandExecutor {
    public ConfigCommand(Initializer initializer) {
        super(initializer, "config");
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> execute() {
        return literal(name()).executes(a -> 1).then(argument("file", new ConfigFileArgument()).executes(a -> 1).then(literal("reload").executes(context -> {
            Config configFile = context.getArgument("file", Config.class);
            if (configFile == null) {
                context.getSource().getSender().sendMessage("No config file specified.");
                return 0;
            }
            context.getSource().getSender().sendMessage(text("Reloading config file: " + configFile.name(), NamedTextColor.GREEN));
            ConfigManager.reloadConfig(configFile.integration(), configFile.getClass());
            context.getSource().getSender().sendMessage(text("Success", NamedTextColor.GREEN));
            return 1;
        })).then(literal("get").executes(a -> 1).then(argument("path", StringArgumentType.greedyString()).executes(context -> {
            Config config = context.getArgument("file", Config.class);
            String path = StringArgumentType.getString(context, "path");
            Object value = config.json().get(path);
            context.getSource().getSender().sendMessage(text("Value at path '" + path + "': " + value, NamedTextColor.GREEN));
            return 1;
        }).suggests((provider, builder) -> {
            Config config = provider.getArgument("file", Config.class);
            JSONObject data = config.json();
            for (String key : data.keySet()) {
                builder.suggest(key);
            }
            return builder.buildFuture();
        })))).build();
    }
}
