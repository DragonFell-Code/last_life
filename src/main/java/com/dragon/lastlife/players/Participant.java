package com.dragon.lastlife.players;

import com.quiptmc.core.config.ConfigObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

public class Participant extends ConfigObject {

    public String team;
    public boolean boogie;
    public int lives;

    public Participant(){
    }

    public Participant(String id, String team, int lives) {
        this.id = id;
        this.team = team;
        this.lives = lives;
        this.boogie = false;
    }

    public void addLife() {
        addLife(1);
    }

    public void addLife(int amount) {
        lives = lives + amount;
       updateName();
    }

    public void updateName(){
        if (lives == 0) {
            setColor(NamedTextColor.GRAY);
        }
        if (lives == 1) {
            setColor(NamedTextColor.RED);
        }
        if (lives == 2) {
            setColor(NamedTextColor.YELLOW);
        }
        if (lives == 3) {
            setColor(NamedTextColor.GREEN);
        }
        if (lives > 3) {
            setColor(NamedTextColor.DARK_GREEN);
        }
    }

    public OfflinePlayer player() {
        return Bukkit.getOfflinePlayer(UUID.fromString(id));
    }

    public void setColor(NamedTextColor color) {
        Player player = player().getPlayer();
        if (player != null && player.isOnline()) {
            Component value = text(player.getName(), color);
            player.displayName(value);
            player.playerListName(value);
            player.customName(value);
        }
    }
}
