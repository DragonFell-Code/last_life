package com.dragon.lastlife.listeners;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.utils.Utils;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import static net.kyori.adventure.text.Component.text;

public class PlayerListener implements Listener {

    public PlayerListener(Initializer initializer) {
        initializer.getServer().getPluginManager().registerEvents(this, initializer);
    }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) throws Exception {
        Utils.configs().PARTICIPANT_CONFIG.get(event.getPlayer().getUniqueId()).addLife(-1);
        Utils.configs().PARTICIPANT_CONFIG.save();
//        event.

    }

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {

        Utils.configs().PARTICIPANT_CONFIG.get(event.getPlayer().getUniqueId()).updateName();
        event.getPlayer().sendMessage(Utils.configs().MESSAGE_CONFIG.parse(text("Welcome to Last Life! ${cmd.session.start}", NamedTextColor.GOLD)));
    }
}
