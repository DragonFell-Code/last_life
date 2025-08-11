package com.dragon.lastlife.listeners;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.config.ParticipantConfig;
import com.dragon.lastlife.players.Participant;
import com.dragon.lastlife.utils.Utils;
import com.dragon.lastlife.utils.chat.MessageUtils;
import com.dragon.lastlife.utils.chat.placeholder.PlaceholderUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import static net.kyori.adventure.text.Component.text;

public class PlayerListener implements Listener {

    public PlayerListener(Initializer initializer) {
        initializer.getServer().getPluginManager().registerEvents(this, initializer);
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent e){
        if(e.getPlayer().isOp()){
            String raw = Utils.configs().MESSAGE_CONFIG.plainText(e.message());
            if(raw.startsWith("!")){
                e.setCancelled(true);

                Bukkit.getScheduler().runTaskLater(Utils.initializer(), ()->{
                    String label = raw.substring(1, raw.indexOf(" "));
                    String[] args = raw.substring(raw.indexOf(" ") + 1).split(" ");
                    System.out.println("Command: " + label + " Args: " + String.join(" ", args));
                    if(label.equals("lives")){
                        if(args.length != 2){
                            e.getPlayer().sendMessage(text("Usage: !lives <player> <amount>", NamedTextColor.RED));
                            return;
                        }
                        String playerName = args[0];
                        int amount;
                        try {
                            amount = Integer.parseInt(args[1]);
                        } catch (NumberFormatException ex) {
                            e.getPlayer().sendMessage((text("Invalid number: " + args[1], NamedTextColor.RED)));
                            return;
                        }
                        Participant participant = Utils.configs().PARTICIPANT_CONFIG.get(e.getPlayer().getUniqueId());
                        int lives = participant.lives().edit(amount);
                        MessageUtils messages = Utils.configs().MESSAGE_CONFIG;
                        e.getPlayer().sendMessage(PlaceholderUtils.replace(e.getPlayer(), "Lives: ${lives}"));
                        Utils.configs().PARTICIPANT_CONFIG.save();
                    } else {
                        e.getPlayer().performCommand(label);
                    }
                }, 0);
            }
        }
    }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        ParticipantConfig config = Utils.configs().PARTICIPANT_CONFIG;
        Participant participant = config.get(e.getEntity().getUniqueId());
        if(participant.lives().remove() <= 0){
            e.setCancelled(true);
            e.getPlayer().getWorld().strikeLightningEffect(e.getPlayer().getLocation());
            Bukkit.broadcast(Utils.configs().MESSAGE_CONFIG.get("lastlife.death.elimination", e.getPlayer().name()));
        }
        config.save();
    }

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {

        Utils.configs().PARTICIPANT_CONFIG.get(event.getPlayer().getUniqueId()).lives().update();
        event.getPlayer().sendMessage(Utils.configs().MESSAGE_CONFIG.parse(text("Welcome to Last Life! ${cmd.session.start}", NamedTextColor.GOLD)));
    }
}
