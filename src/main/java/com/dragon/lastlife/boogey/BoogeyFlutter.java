package com.dragon.lastlife.boogey;

import com.dragon.lastlife.players.Participant;
import com.dragon.lastlife.utils.Utils;
import com.quiptmc.core.heartbeat.Flutter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Particle;
import org.bukkit.Color;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

import static net.kyori.adventure.text.Component.text;

public class BoogeyFlutter implements Flutter {

    long lastHeartbeat = 0;

    private final BossBar boogeyBossBar = BossBar.bossBar(text("You are the Boogeyman!", NamedTextColor.RED), 1f, BossBar.Color.RED, BossBar.Overlay.PROGRESS);

    public BoogeyFlutter() {
        Utils.initializer().integration().log("BoogeyFlutter", "Initialized");
    }

    @Override
    public boolean run() {
        long now = System.currentTimeMillis();
        if (lastHeartbeat + 50 <= now) {
            lastHeartbeat = now;
            for (Participant participant : Utils.configs().PARTICIPANT_CONFIG().cache.values()) {
                Player player = participant.player().getPlayer();
                if (player == null) continue;
                if (!participant.boogey || participant.settings.get("boogey_boss_bar").value().equalsIgnoreCase("false")) {
                    //No need for complicated checks, make sure they can't see the bassbar and move on.
                    player.hideBossBar(boogeyBossBar);
                    continue;
                }
                if (participant.settings.get("boogey_particles").value().equalsIgnoreCase("true")) {
                    // Show small red redstone particles that disappear quickly around the player's upper body
                    // REDSTONE particles require DustOptions for color and size
                    Particle.DustOptions redDust = new Particle.DustOptions(Color.RED, 0.6f);

                    player.spawnParticle(
                            Particle.DUST,
                            player.getLocation().add(0, 1.0, 0), // around chest/head height
                            1,      // small count for subtle effect
                            0.3, 0.5, 0.3, // small spread around the player
                            0,       // speed not used for REDSTONE
                            redDust  // color and size
                    );
                }
                if (participant.settings.get("boogey_boss_bar").value().equalsIgnoreCase("true")) {
                    player.showBossBar(boogeyBossBar);
                }
            }
        }
        return true;
    }
}
