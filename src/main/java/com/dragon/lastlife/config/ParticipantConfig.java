package com.dragon.lastlife.config;

import com.dragon.lastlife.players.Participant;
import com.dragon.lastlife.utils.Utils;
import com.quiptmc.core.QuiptIntegration;
import com.quiptmc.core.config.Config;
import com.quiptmc.core.config.ConfigMap;
import com.quiptmc.core.config.ConfigTemplate;
import com.quiptmc.core.config.ConfigValue;
import com.quiptmc.core.discord.WebhookManager;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

@ConfigTemplate(name = "participants", ext = ConfigTemplate.Extension.JSON)
public class ParticipantConfig extends Config {

    private final BoogeymenManager boogeymen = new BoogeymenManager();
    @ConfigValue
    public ConfigMap<Participant> cache = new ConfigMap<>();
    @ConfigValue
    public int queued_boogeymen = 0;

    public ParticipantConfig(File file, String name, ConfigTemplate.Extension extension, QuiptIntegration integration) {
        super(file, name, extension, integration);
    }

    /**
     * Get a participant by their donor drive id from storage.
     * @param participantId The donor drive id of the participant.
     * @return {@link Participant} if found, null otherwise.
     */
    public Participant get(int participantId) {
        for (Participant participant : cache.values()) {
            if (participant.donorDriveId == participantId) {
                return participant;
            }
        }
        return null;
    }

    public Participant get(UUID uuid) {
        if (cache.contains(uuid.toString())) {
            return cache.get(uuid.toString());
        }
        Participant participant = new Participant(uuid.toString(), "none", 3, 0);
        cache.put(participant);
        save();
        return participant;
    }

    public BoogeymenManager boogeymen() {
        return boogeymen;
    }

    public void link(UUID uuid, long participantId) {
        Participant participant = get(uuid);
        participant.donorDriveId = participantId;
        cache.put(participant);
        save();
    }

    public class BoogeymenManager {


        public void roll() {
            roll(0);
        }

        public void roll(int amount) {
            List<Participant> selected = new ArrayList<>();
            int attempts = 0;
            amount = amount + queued_boogeymen;
            queued_boogeymen = 0;
            while (selected.size() < amount) {
                attempts++;
                if (attempts > 100) {
                    Utils.initializer().integration().warn("BoogeymenManager", "Failed to select boogeyman after 100 attempts. Selected " + selected.size() + " out of " + amount);
                    break;
                }
                int index = (int) (Math.random() * cache.size());
                Participant participant = cache.values().toArray(new Participant[0])[index];
                if (participant.lives().lives() <= 1) continue;
                if (selected.contains(participant)) continue;
                if (participant.boogey) continue;
                if (participant.player() == null) continue;
                //todo add checks for if they are online, not modded, and haven't won in the last 7 days [ai]
                selected.add(participant);
            }
            if (selected.size() < amount) {
                Utils.initializer().integration().warn("BoogeymenManager", "Failed to select enough boogeymen. Selected " + selected.size() + " out of " + amount);
                queued_boogeymen = queued_boogeymen + (amount - selected.size());
                save();
            }
            countdown(selected);
        }


        private void countdown(List<Participant> selected) {
            String key = selected.size() != 1 ? "lastlife.boogey.roll.multiple" : "lastlife.boogey.roll";
            Bukkit.broadcast(Utils.configs().MESSAGE_CONFIG.get(key, String.valueOf(selected.size())));
            Bukkit.getScheduler().runTaskLater(Utils.initializer(), new Runnable() {

                int seconds_remaining = 4;

                //Cheat Sheet:
                // 4 = "3..."
                // 3 = "2..."
                // 2 = "1..."
                // 1 = "You are..."
                // 0 = "NOT the Boogeyman." OR "The Boogeyman."
                @Override
                public void run() {
                    TextColor color;
                    switch (seconds_remaining) {
                        case 4 -> color = NamedTextColor.GREEN;
                        case 3 -> color = NamedTextColor.YELLOW;
                        case 2 -> color = NamedTextColor.RED;
                        default -> color = NamedTextColor.GOLD;
                    }
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Participant participant = Utils.configs().PARTICIPANT_CONFIG().get(player.getUniqueId());
                        if (participant == null || participant.lives().lives() <= 0) continue;
                        if (seconds_remaining >= 2) {
                            player.showTitle(Title.title(text(seconds_remaining - 1 + "...", color).style(Style.style().decorate(TextDecoration.BOLD).color(color)), text(""), 5, 10, 5));
                        } else if (seconds_remaining == 1) {
                            player.showTitle(Title.title(text("You are...", color), text(""), 5, 10, 5));
                        } else if (seconds_remaining == 0) {
                            if (selected.stream().anyMatch(p -> p.id.equals(player.getUniqueId().toString()))) {
                                player.showTitle(Title.title(text("The Boogeyman.", NamedTextColor.RED), text(""), 5, 10, 20));
                            } else {
                                player.showTitle(Title.title(text("NOT the Boogeyman.", NamedTextColor.GREEN), text(""), 5, 10, 20));
                            }
                        }

                    }
                    if (seconds_remaining >= 0) {
                        seconds_remaining--;
                        Bukkit.getScheduler().runTaskLater(Utils.initializer(), this, 30L);
                    } else {
                        for (Participant participant : selected) {
                            setBoogey(participant, true);
                            Player player = participant.player().getPlayer();
                            if (player != null && player.isOnline()) {
                                player.sendMessage(Utils.configs().MESSAGE_CONFIG.get("lastlife.boogey.set", player.getName(), participant.boogey));
                            }
                        }
                        save();
                    }

                }
            }, 20L);
        }

        public void setBoogey(Participant participant, boolean boogey) {
            participant.boogey = boogey;
            participant.sync();
            if (boogey) {
                if (WebhookManager.get("boogeymen") != null)
                    Utils.genericWebhook("boogeymen", new Color(0xD27330), "Boogeyman Selected!", "https://mc-heads.net/head/" + participant.id + "/left.png", participant.player().getName() + " has been selected as a boogeyman!");

            }
        }

        public void queue() {
            queue(1);
        }

        public void queue(int amount) {
            queued_boogeymen = queued_boogeymen + amount;
            save();
        }
    }
}
