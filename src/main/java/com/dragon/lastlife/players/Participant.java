package com.dragon.lastlife.players;

import com.dragon.lastlife.utils.net.MessageChannelHandler;
import com.dragon.lastlife.utils.Utils;
import com.quiptmc.core.config.ConfigObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

public class Participant extends ConfigObject {

    private final LifeManager lifeManager = new LifeManager();
    public String team;
    public boolean boogie;
    public int lives;

    public Participant() {
    }

    public Participant(String id, String team, int lives) {
        this.id = id;
        this.team = team;
        this.lives = lives;
        this.boogie = false;
    }

    public LifeManager lives() {
        return lifeManager;
    }

    public void sync() {
        lives().update();
        Player player = player().getPlayer();
        if (player == null || !player.isOnline()) return;
        MessageChannelHandler handler = Utils.channelMessageHandler();
        handler.send("data", player, (byte) 1, json().toString(), 1);
//        handler.send(Utils.initializer(), player(), json().toString(), 1, (byte) 1);
    }

    public boolean spectating() {
        Player player = player().getPlayer();
        return player != null && player.isOnline() && player.getGameMode() == GameMode.SPECTATOR;
    }

    private void spectate() {
        Player player = player().getPlayer();
        if (player != null && player.isOnline()) {
            player.setGameMode(GameMode.SPECTATOR);
            player.setHealth(player.getHealthScale());
        }
    }


    public OfflinePlayer player() {
        return Bukkit.getOfflinePlayer(UUID.fromString(id));
    }

    public void color(NamedTextColor color) {
        Player player = player().getPlayer();
        if (player != null && player.isOnline()) {
            Component value = text(player.getName(), color);
            player.displayName(value);
            player.playerListName(value);
            player.customName(value);
        }
    }

    public class LifeManager {

        public void update() {
            if (lives <= 0) {
                color(NamedTextColor.GRAY);
                lives = 0;
                spectate();
            } else if (spectating()) {
                revive();
            }
            if (lives == 1) {
                color(NamedTextColor.RED);
            }
            if (lives == 2) {
                color(NamedTextColor.YELLOW);
            }
            if (lives == 3) {
                color(NamedTextColor.GREEN);
            }
            if (lives > 3) {
                color(NamedTextColor.DARK_GREEN);
            }
        }

        private void revive() {
            Player player = player().getPlayer();
            if (player != null && player.isOnline()) {
                player.setGameMode(GameMode.SURVIVAL);
                player.setHealth(player.getHealthScale());
            }
        }

        public int add() {
            return edit(1);
        }

        public int add(int amount) {
            return edit(amount);
        }

        public int remove() {
            return edit(-1);
        }

        public int remove(int amount) {
            return edit(-amount);
        }

        public int edit(int amount) {
            int lives = Participant.this.lives + amount;
            return set(lives);
        }

        public int set(int lives) {
            Participant.this.lives = lives;
            update();
            return lives();
        }

        public int lives() {
            return Participant.this.lives;
        }
    }
}
