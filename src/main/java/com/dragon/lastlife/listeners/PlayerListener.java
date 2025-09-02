package com.dragon.lastlife.listeners;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.config.DonationConfig;
import com.dragon.lastlife.config.ParticipantConfig;
import com.dragon.lastlife.donations.Donation;
import com.dragon.lastlife.players.Participant;
import com.dragon.lastlife.utils.Utils;
import com.dragon.lastlife.utils.chat.MessageUtils;
import com.dragon.lastlife.utils.chat.placeholder.PlaceholderUtils;
import com.dragon.lastlife.world.Dungeon;
import com.quiptmc.core.config.ConfigManager;
import com.quiptmc.core.utils.TaskScheduler;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import static net.kyori.adventure.text.Component.text;

public class PlayerListener implements Listener {

    public PlayerListener(Initializer initializer) {
        initializer.getServer().getPluginManager().registerEvents(this, initializer);
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent e) {
        if (e.getPlayer().isOp()) {
            String raw = Utils.configs().MESSAGE_CONFIG.plainText(e.message());
            if (raw.startsWith("!")) {
                e.setCancelled(true);

                Bukkit.getScheduler().runTaskLater(Utils.initializer(), () -> {
                    int indexOfSpace = raw.contains(" ") ? raw.indexOf(" ") : raw.length();
                    String label = raw.substring(1, indexOfSpace);
                    String argsRaw = raw.length() == indexOfSpace ? "" : raw.substring(indexOfSpace + 1);
                    String[] args = argsRaw.contains(" ") ? argsRaw.split(" ") : new String[]{argsRaw};
                    System.out.println("Command: " + label + " Args: " + String.join(" ", args));
                    if (label.equals("packet")) {
                        Utils.configs().PARTICIPANT_CONFIG().get(e.getPlayer().getUniqueId()).sync();
                        e.getPlayer().sendMessage(text("Sent packet!", NamedTextColor.GREEN));
                    }

                    if(label.equalsIgnoreCase("dungeon")){
                        Dungeon dungeon = Utils.configs().DUNGEON_MANAGER.create("test");
                        dungeon.generate("test", 0,100,0);
                        e.getPlayer().teleport(new Location(dungeon.world(), 0, 150, 0));
                    }

                    if(label.equalsIgnoreCase("config")){
                        ConfigManager.reloadConfig(Utils.initializer().integration(), DonationConfig.class);
                    }

                    if(label.equalsIgnoreCase("donation")){
                        JSONObject json = new JSONObject()
                                .put("displayName", "Test Donor")
                                .put("donorId", "270CB800398A911A")
                                .put("links", new JSONObject()
                                        .put("recipient", "https://www.extra-life.org/participants/548726")
                                        .put("donate", "https://www.extra-life.org/participants/548726/donate"))
                                .put("isRegFee", false)
                                .put("eventID", 559)
                                .put("createdDateUTC", "2025-02-01T18:55:51.460+0000")
                                .put("recipientName", "Test Participant (Elkhorn95)")
                                .put("recipientImageURL", "https://donordrivecontent.com/extralife/images/$avatars$/constituent_09429FD9-D538-F066-0D0C8F329156DFD1.jpg?v=1756493068380")
                                .put("participantID", 548926)
                                .put("amount", "5.00")
                                .put("avatarImageURL", "https://donordrivecontent.com/extralife/images/$avatars$/constituent_default_100.jpg?v=1756493068380")
                                .put("teamID", Utils.configs().DONATION_CONFIG().team_id)
                                .put("donationID", "53DFA757C375170D")
                                .put("incentiveID", "D0AD363E-BA37-AD5A-15AFC5541F245399")
                                .put("message", "what if you have to deal with it as a fish fight instead!!!");
                        Donation donation = new Donation(json);
                        Utils.configs().DONATION_CONFIG().process(donation);
//                        donation.process();
                    }

                    if (label.equals("lives")) {
                        if (args.length != 2) {
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
                        Participant participant = Utils.configs().PARTICIPANT_CONFIG().get(e.getPlayer().getUniqueId());
                        int lives = participant.lives().edit(amount);
                        MessageUtils messages = Utils.configs().MESSAGE_CONFIG;
                        e.getPlayer().sendMessage(PlaceholderUtils.replace(e.getPlayer(), "Lives: ${lives}"));
                        Utils.configs().PARTICIPANT_CONFIG().save();
                    } else {
                        e.getPlayer().performCommand(label);
                    }
                }, 0);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        ParticipantConfig config = Utils.configs().PARTICIPANT_CONFIG();
        Participant participant = config.get(e.getEntity().getUniqueId());
        if (participant.lives().remove() <= 0) {
            e.setCancelled(true);
            e.getPlayer().getWorld().strikeLightningEffect(e.getPlayer().getLocation());
            Bukkit.broadcast(Utils.configs().MESSAGE_CONFIG.get("lastlife.death.elimination", e.getPlayer().name()));
        }
        if(e.getDamageSource().getCausingEntity() != null && e.getDamageSource().getCausingEntity() instanceof Player killer){
            Participant killerParticipant = config.get(killer.getUniqueId());
            if(killerParticipant != null){
                if(killerParticipant.boogey) {
                    killer.sendMessage(Utils.configs().MESSAGE_CONFIG.get("lastlife.boogey.cured"));
                    killerParticipant.boogey = false;
                }
//                killerParticipant.stats.deaths++;
//                killerParticipant.stats.kills++;
                killerParticipant.sync();
            }

        }
        config.save();

    }

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent e) {
        TaskScheduler.scheduleAsyncTask(() -> {
            Utils.configs().PARTICIPANT_CONFIG().get(e.getPlayer().getUniqueId()).sync();

        }, 500, TimeUnit.MILLISECONDS);
        e.getPlayer().sendMessage(Utils.configs().MESSAGE_CONFIG.parse(text("Welcome to Last Life! ${cmd.session.start}", NamedTextColor.GOLD)));
        Participant participant = Utils.configs().PARTICIPANT_CONFIG().get(e.getPlayer().getUniqueId());
        if(participant.settings.get("boogey_particles") == null){
            Participant.ConfigString string = new Participant.ConfigString("boogey_particles", "flame");
            participant.settings.put(string);
        }
        JSONObject json = participant.settings.json();
        e.getPlayer().sendMessage(participant.settings.get("boogey_particles").toString());
        Utils.configs().PARTICIPANT_CONFIG().save();
        System.out.println(json.toString(2));
    }
}
