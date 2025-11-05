package com.dragon.lastlife.commands.executor.argument;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public abstract class CustomArgumentConverter<R, I> implements CustomArgumentType.Converted<@NotNull R, @NotNull I> {

    public CompletableFuture<Suggestions> onlySimilar(String[] values, String input, SuggestionsBuilder builder) {
        return getSuggestionsCompletableFuture(values, input, builder);
    }

    public static CompletableFuture<Suggestions> getSuggestionsCompletableFuture(String[] values, String input, SuggestionsBuilder builder) {
        if (input == null || input.isBlank()) {
            for (String v : values) {
                builder.suggest(v);
            }
            return builder.buildFuture();
        }
        for (String v : values) {
            if (v.toLowerCase().startsWith(input.toLowerCase())) {
                builder.suggest(v);
            }
        }
        return builder.buildFuture();
    }
}