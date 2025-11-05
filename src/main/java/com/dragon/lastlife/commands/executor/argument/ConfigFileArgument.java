package com.dragon.lastlife.commands.executor.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.quiptmc.core.config.Config;
import com.quiptmc.core.config.ConfigManager;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ConfigFileArgument extends CustomArgumentConverter<@NotNull Config, @NotNull String> {


    @Override
    public Config convert(@NotNull String nativeType) {
        return ConfigManager.getConfig(nativeType.replaceAll("\"", ""));
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }

    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {

        for (String key : ConfigManager.getAll()) {
            builder.suggest("\"" + key + "\"");
        }

        return builder.buildFuture();
    }

}
