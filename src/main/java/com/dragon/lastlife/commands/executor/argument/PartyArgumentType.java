package com.dragon.lastlife.commands.executor.argument;

import com.dragon.lastlife.party.Party;
import com.dragon.lastlife.utils.Utils;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class PartyArgumentType extends CustomArgumentConverter<@NotNull Party, @NotNull String> {
    @Override
    public @NotNull Party convert(@NotNull String s) throws CommandSyntaxException {
        return Utils.configs().PARTY_CONFIG().get(s);
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
        for (Party party : Utils.configs().PARTY_CONFIG().parties.values()) {
            values[i] = party.id();
            i=i+1;
        }
        return onlySimilar(values, input, builder);
    }
}
