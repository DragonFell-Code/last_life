package com.dragon.lastlife.commands.executor.argument;

import com.dragon.lastlife.config.object.ConfigLocation;
import com.dragon.lastlife.utils.Utils;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class PoiArgumentType extends CustomArgumentConverter<@NotNull ConfigLocation, @NotNull String> {
    @Override
    public @NotNull ConfigLocation convert(@NotNull String s) {
        return Utils.configs().POI_CONFIG().pois.get(s);
    }

    @Override
    public ArgumentType<@NotNull String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {

        String input = builder.getInput().substring(builder.getStart());
        String[] values = new String[Utils.configs().PARTY_CONFIG().parties.size()];
        int i = 0;
        for (ConfigLocation loc : Utils.configs().POI_CONFIG().pois.values()) {
            values[i] = loc.id();
            i=i+1;
        }
        return onlySimilar(values, input, builder);
    }
}
