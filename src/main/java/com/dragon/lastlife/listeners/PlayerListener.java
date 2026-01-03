package com.dragon.lastlife.listeners;

import com.dragon.lastlife.config.ParticipantConfig;
import com.dragon.lastlife.nms.CustomFox;
import com.dragon.lastlife.party.Party;
import com.dragon.lastlife.players.InventorySnapshot;
import com.dragon.lastlife.players.Participant;
import com.dragon.lastlife.utils.Utils;
import com.dragon.lastlife.world.DungeonManager;
import com.quiptmc.core.utils.TaskScheduler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.awt.*;
import java.util.concurrent.TimeUnit;

import static net.kyori.adventure.text.Component.text;
import static org.bukkit.GameMode.CREATIVE;

public class PlayerListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if (((CraftEntity) event.getRightClicked()).getHandle() instanceof CustomFox fox) {
            if (fox.getParty() == null) return;
            Participant participant = Utils.configs().PARTICIPANT_CONFIG().get(event.getPlayer().getUniqueId());

            if (participant == null) {
                event.getPlayer().sendMessage(text("You are not a participant!", NamedTextColor.RED));
                return;
            }
            Party party = Utils.configs().PARTY_CONFIG().get(participant).orElse(null);
            if (party == null) {
                event.getPlayer().sendMessage(text("You are not in a party!", NamedTextColor.RED));
                return;
            }
            if (!fox.getParty().equals(party.id())) {
                event.getPlayer().sendMessage(text("Your party does not own this fox!", NamedTextColor.RED));
                return;
            }

            if (fox.getState().equals(CustomFox.State.WAITING)) {
                fox.setState(CustomFox.State.DROPPING_OFF);
                Fox bukkitFox = (Fox) fox.getBukkitEntity();
                Location target = event.getPlayer().getLocation();
                fox.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1);
                bukkitFox.setSitting(false);
                bukkitFox.setLeaping(true);
                fox.getJumpControl().jump();
                bukkitFox.getWorld().dropItem(bukkitFox.getLocation(), bukkitFox.getEquipment().getItemInMainHand());
                bukkitFox.clearActiveItem();

                Bukkit.getScheduler().runTaskLater(Utils.initializer(), () -> {
                    World world = bukkitFox.getWorld();
                    world.spawnParticle(Particle.CLOUD, bukkitFox.getLocation(), 10);

                    bukkitFox.remove();
                }, 20);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getPlayer();
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ParticipantConfig config = Utils.configs().PARTICIPANT_CONFIG();
        Participant participant = config.get(player.getUniqueId());

        if (participant == null || player.getGameMode() == CREATIVE) {
            return;
        }

        int lives = participant.lives().remove();

        Utils.initializer().getComponentLogger().info("{} lost a life !", player.getName());

        Utils.genericWebhook("death", new Color(0xFC486C), "Death", "https://mc-heads.net/combo/" + player.getUniqueId(), Utils.configs().MESSAGE_CONFIG.plainText(e.deathMessage()) + (lives <= 0 ? " (Elimination)" : ""));

        // Boogey curing
        if (e.getDamageSource().getCausingEntity() != null && e.getDamageSource().getCausingEntity() instanceof Player killer) {
            if (!killer.equals(player)) {
                Participant killerParticipant = config.get(killer.getUniqueId());
                if (killerParticipant != null) {
                    if (killerParticipant.boogey) {
                        config.boogeymen().setBoogey(killerParticipant, false);
                        Utils.genericWebhook("boogey", new Color(0x81FC01), "Boogey Cured", "https://mc-heads.net/combo/" + killer.getUniqueId(), killer.getName() + " has been cured!");
                    }
                    killerParticipant.sync();
                }
            }
        }

        // Last life
        if (lives <= 0) {
            e.setCancelled(true);
            player.getWorld().strikeLightningEffect(e.getPlayer().getLocation());
            Component deathMessage = e.deathMessage();
            if (deathMessage != null)
                Bukkit.broadcast(deathMessage);
            Bukkit.broadcast(Utils.configs().MESSAGE_CONFIG.get("lastlife.death.elimination", e.getPlayer().name()));

        } else { // Player is still alive
            DungeonManager manager = Utils.configs().DUNGEON_MANAGER;

            // Dungeon Death with keepInventory ON
            if (player.getWorld().equals(manager.dungeon_world) && e.getKeepInventory()) {
                InventorySnapshot.applyPlayerInventorySnapshot(serverPlayer, e);
            }
        }

        config.save();
    }

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent e) {
        TaskScheduler.scheduleAsyncTask(() -> Utils.configs().PARTICIPANT_CONFIG().get(e.getPlayer().getUniqueId()).sync(), 500, TimeUnit.MILLISECONDS);
        e.getPlayer().sendMessage(Utils.configs().MESSAGE_CONFIG.parse(text("Welcome to Last Life!", NamedTextColor.GOLD)));
    }
}
