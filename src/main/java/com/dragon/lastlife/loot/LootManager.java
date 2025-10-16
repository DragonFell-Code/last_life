package com.dragon.lastlife.loot;

import com.dragon.lastlife.nms.CustomFox;
import com.dragon.lastlife.nms.NmsEntityFactory;
import com.dragon.lastlife.players.Participant;
import com.dragon.lastlife.utils.Utils;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

public class LootManager {

    //todo change Participant to Party
    public void generate(LootType type, Participant participant) {
        switch (type) {
            case BUNDLE -> {
                OfflinePlayer offlinePlayer = participant.player();
                if (offlinePlayer == null) return;
                if (offlinePlayer.getPlayer() == null) return;
                Player player = offlinePlayer.getPlayer();
                World world = player.getWorld();

                // Spawn a little above and in front, then nudge up until air
                Location spawn = player.getLocation().clone().add(0, 3, 10);
                while (spawn.getBlock().getType().isSolid()) spawn.add(0, 1, 0);

                // Set a target – here we use the player's current location,
                // but this can be any arbitrary Location you pass in
                Location target = player.getLocation().clone();

                try {
                    // Spawn our custom NMS fox with overridden AI that walks to target
                    CustomFox fox = NmsEntityFactory.spawn(spawn, CustomFox.class);
                    Vec3 vecTarget = new Vec3(target.x(), target.y(), target.z());
                    fox.setTarget(vecTarget);

                    // Mark with PDC so we can restore after restarts
                    org.bukkit.entity.Fox bukkitFox = (org.bukkit.entity.Fox) fox.getBukkitEntity();
                    CustomFox.writePersistentData(bukkitFox, CustomFox.State.DELIVERING, vecTarget);

                    // Poll until close enough, then sit for aesthetics (NMS already stops moving)
                    Bukkit.getScheduler().runTaskTimer(Utils.initializer(), task -> {
                        if (!fox.getBukkitEntity().isValid() || fox.getBukkitEntity().isDead()) {
                            task.cancel();
                            return;
                        }
                        double distSq = fox.getBukkitEntity().getLocation().distanceSquared(target);
                        if (distSq <= 2.25) { // within 1.5 blocks
                            ((org.bukkit.entity.Fox) fox.getBukkitEntity()).setSitting(true);
                            // Update PDC state to WAITING
                            CustomFox.writePersistentData((org.bukkit.entity.Fox) fox.getBukkitEntity(), CustomFox.State.WAITING, vecTarget);
                            task.cancel();
                        }
                    }, 10L, 10L);
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                         IllegalAccessException e) {
                    throw new RuntimeException(e);
                }


            }
        }

    }

    public enum LootType {
        BUNDLE,
        SHULKER;
    }
}
