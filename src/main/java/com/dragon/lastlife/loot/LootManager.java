package com.dragon.lastlife.loot;

import com.dragon.lastlife.nms.CustomFox;
import com.dragon.lastlife.nms.NmsEntityFactory;
import com.dragon.lastlife.players.Participant;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

public class LootManager {
    // TODO: change Participant to Party
    public void generate(LootType type, Participant participant) {
        switch (type) {
            case BUNDLE -> {
                OfflinePlayer offlinePlayer = participant.player();
                if (offlinePlayer == null) return;
                if (offlinePlayer.getPlayer() == null) return;
                Player player = offlinePlayer.getPlayer();

                // Spawn a little above and in front, then nudge up until air
                // TODO: Randomize it ?
                Location spawn = player.getLocation().clone().add(0, 3, 10);
                while (spawn.getBlock().getType().isSolid()) {
                    // TODO: Also check for valid spawn ? (eg: not in a wall)
                    // Does MC already has methods to locate a safe-spawn position ?
                    spawn.add(0, 1, 0);
                }

                // Set a target – here we use the player's current location,
                // but this can be any arbitrary Location you pass in
                Location target = player.getLocation().clone();

                try {
                    // Spawn our custom NMS fox with overridden AI that walks to target
                    CustomFox fox = NmsEntityFactory.spawn(spawn, CustomFox.class);
                    Vec3 vecTarget = new Vec3(target.x(), target.y(), target.z());
                    fox.deliverTo(vecTarget);
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
