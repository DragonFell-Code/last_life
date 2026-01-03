package com.dragon.lastlife.commands.executor;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.commands.CommandExecutor;
import com.dragon.lastlife.utils.Utils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.quiptmc.core.discord.Webhook;
import com.quiptmc.core.discord.WebhookManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.Iterator;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class WebhookCommand extends CommandExecutor {

    public WebhookCommand(Initializer initializer) {
        super(initializer, "webhook");
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> arguments() {
        return literal(name())
                .executes(context -> showUsage(context, "lastlife.admin"))
                .then(literal("add")
                        .executes(context -> showUsage(context, "lastlife.admin"))
                        .then(argument("id", StringArgumentType.word())
                                .then(argument("token", StringArgumentType.word())
                                        .then(argument("channel", StringArgumentType.word())
                                                .executes(context -> {
                                                    CommandSender sender = context.getSource().getSender();
                                                    if (!sender.hasPermission("lastlife.admin"))
                                                        return logError(context, "You do not have permission to use this command.");

                                                    String id = StringArgumentType.getString(context, "id");
                                                    String token = StringArgumentType.getString(context, "token");
                                                    String channel = StringArgumentType.getString(context, "channel");

                                                    // Register with runtime manager first
                                                    WebhookManager.add(id, channel, token);
                                                    Webhook wh = WebhookManager.get(id);
                                                    if (wh == null)
                                                        return logError(context, "Failed to create webhook. Please check your inputs.");

                                                    // Persist to config and save
                                                    Utils.configs().WEBHOOKS_CONFIG().webhooks.put(wh);
                                                    Utils.configs().WEBHOOKS_CONFIG().save();

                                                    sender.sendMessage(net.kyori.adventure.text.Component.text("Added webhook '" + id + "' and saved to config."));
                                                    return 1;
                                                })))))
                .then(literal("remove")
                        .executes(context -> showUsage(context, "lastlife.admin"))
                        .then(argument("id", StringArgumentType.word())
                                .executes(context -> {
                                    CommandSender sender = context.getSource().getSender();
                                    if (!sender.hasPermission("lastlife.admin"))
                                        return logError(context, "You do not have permission to use this command.");

                                    String id = StringArgumentType.getString(context, "id");

                                    // Try treat provided id as channel name first
                                    boolean removed = false;
                                    Webhook existing = Utils.configs().WEBHOOKS_CONFIG().webhooks.get(id);
                                    if (existing != null) {
                                        Utils.configs().WEBHOOKS_CONFIG().webhooks.remove(id);
                                        removed = true;
                                    } else {
                                        // Fallback: search by underlying discord id
                                        Iterator<Webhook> it = Utils.configs().WEBHOOKS_CONFIG().webhooks.values().iterator();
                                        String keyToRemove = null;
                                        while (it.hasNext()) {
                                            Webhook w = it.next();
                                            try {
                                                // Try common accessor names
                                                String discordId;
                                                try {
                                                    discordId = (String) w.getClass().getMethod("id").invoke(w);
                                                } catch (NoSuchMethodException nsme) {
                                                    discordId = (String) w.getClass().getMethod("webhookId").invoke(w);
                                                }
                                                if (id.equalsIgnoreCase(discordId)) {
                                                    keyToRemove = w.name();
                                                    break;
                                                }
                                            } catch (Exception ignore) {
                                                // If reflection fails, skip
                                            }
                                        }
                                        if (keyToRemove != null) {
                                            Utils.configs().WEBHOOKS_CONFIG().webhooks.remove(keyToRemove);
                                            removed = true;
                                        }
                                    }

                                    if (!removed) {
                                        return logError(context, "Webhook not found by channel or id: " + id);
                                    }

                                    Utils.configs().WEBHOOKS_CONFIG().save();
                                    sender.sendMessage(net.kyori.adventure.text.Component.text("Removed webhook '" + id + "' from config."));
                                    return 1;
                                })));
    }
}
