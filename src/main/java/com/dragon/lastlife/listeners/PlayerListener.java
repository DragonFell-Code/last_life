package com.dragon.lastlife.listeners;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.config.DonationConfig;
import com.dragon.lastlife.config.ParticipantConfig;
import com.dragon.lastlife.donations.Donation;
import com.dragon.lastlife.nms.CustomFox;
import com.dragon.lastlife.players.Participant;
import com.dragon.lastlife.utils.Utils;
import com.dragon.lastlife.utils.chat.MessageUtils;
import com.dragon.lastlife.utils.chat.placeholder.PlaceholderUtils;
import com.quiptmc.core.config.ConfigManager;
import com.quiptmc.core.config.objects.ConfigString;
import com.quiptmc.core.utils.TaskScheduler;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.json.JSONObject;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static net.kyori.adventure.text.Component.text;

public class PlayerListener implements Listener {

    public PlayerListener(Initializer initializer) {
        initializer.getServer().getPluginManager().registerEvents(this, initializer);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event){
        if(((CraftEntity) event.getRightClicked()).getHandle() instanceof CustomFox fox){
            if(fox.state().equals(CustomFox.State.WAITING)){
                fox.state(CustomFox.State.DROPPING_OFF);
                Fox bukkitFox = (Fox)fox.getBukkitEntity();
                // Update PDC to persist state change across restart
                net.minecraft.world.phys.Vec3 vec = CustomFox.readTarget(bukkitFox);
                CustomFox.writePersistentData(bukkitFox, CustomFox.State.DROPPING_OFF, vec);

                bukkitFox.setSitting(false);
                bukkitFox.setLeaping(true);
                bukkitFox.getWorld().dropItem(bukkitFox.getLocation(), bukkitFox.getEquipment().getItemInMainHand());
                bukkitFox.clearActiveItem();
                Bukkit.getScheduler().runTaskLater(Utils.initializer(), ()-> {
                    World world = bukkitFox.getWorld();
                    world.spawnParticle(Particle.CLOUD, bukkitFox.getLocation(), 10);

                    bukkitFox.remove();

                },20);

            }
        }
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
                    if (label.equals("packet")) {
                        Utils.configs().PARTICIPANT_CONFIG().get(e.getPlayer().getUniqueId()).sync();
                        e.getPlayer().sendMessage(text("Sent packet!", NamedTextColor.GREEN));
                    }

                    if (label.equalsIgnoreCase("dungeon")) {
                        String world_name = args.length >= 1 ? args[0] : Date.from(Instant.EPOCH.plusMillis(System.currentTimeMillis())).toString().replace(":", "-");
                        ChunkPos pos = new ChunkPos(0, 0);
                        Utils.configs().DUNGEON_MANAGER.create(world_name, pos, (dungeon -> {
                            BlockPos origin = dungeon.origin;
                            Player player = e.getPlayer();
                            ServerPlayer serverPlayer = ((CraftPlayer)player).getHandle();
                            ServerLevel level = ((CraftWorld) dungeon.world).getHandle();

                            BlockPos spawn_pos = origin;
                            LevelChunk chunk = level.getChunkAt(origin);

                            // Origin is in the middle of the room vertically, bring it down to a solid surface so players dont
                            // spawn midair. They could still spawn in the middle of a pillar tho.
                            while (!level.loadedAndEntityCanStandOn(spawn_pos.below(), serverPlayer)) {
                                spawn_pos = spawn_pos.below();
                                // If we scanned all the way to the void and no valid block was found, just use the origin.
                                if (level.isOutsideBuildHeight(spawn_pos)) {
                                    spawn_pos = origin;
                                    player.sendMessage(text("Reset to Origin!", NamedTextColor.GREEN));
                                    break;
                                }
                            }

                            Vec3 final_pos = spawn_pos.getBottomCenter();

                            player.teleport(new Location(dungeon.world, final_pos.x(), final_pos.y(), final_pos.z()));
                        }));
                    }

                    if (label.equalsIgnoreCase("config")) {
                        ConfigManager.reloadConfig(Utils.initializer().integration(), DonationConfig.class);
                    }

                    if (label.equalsIgnoreCase("donation")) {
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
                                .put("participantID", 548726)
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
        if (e.getDamageSource().getCausingEntity() != null && e.getDamageSource().getCausingEntity() instanceof Player killer) {
            Participant killerParticipant = config.get(killer.getUniqueId());
            if (killerParticipant != null) {
                if (killerParticipant.boogey) {
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
        if (participant.settings.get("boogey_particles") == null) {
            ConfigString string = new ConfigString("boogey_particles", "flame");
            participant.settings.put(string);
        }
        JSONObject json = participant.settings.json();
        e.getPlayer().sendMessage(participant.settings.get("boogey_particles").toString());
        Utils.configs().PARTICIPANT_CONFIG().save();
    }
}
