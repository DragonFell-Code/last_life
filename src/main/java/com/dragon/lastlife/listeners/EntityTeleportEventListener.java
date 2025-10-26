package com.dragon.lastlife.listeners;

import com.destroystokyo.paper.event.player.PlayerTeleportEndGatewayEvent;
import com.dragon.lastlife.players.InventorySnapshot;
import com.dragon.lastlife.utils.Utils;
import com.dragon.lastlife.world.DungeonManager;
import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerPortalEvent;

public class EntityTeleportEventListener implements Listener {
    @EventHandler
    // This gets spammed for nether portals, but allows for EndGateway detection without an exit_portal:[X, Y, Z] attribute
    public void onEntityPortalEnter(EntityPortalEnterEvent event) {
        Utils.initializer().getComponentLogger().error("onEntityPortalEnter");
    }

    @EventHandler
    // Nether / End Portal blocks
    public void onPlayerPortal(PlayerPortalEvent event) {
        Utils.initializer().getComponentLogger().error("onPlayerPortal");
    }

    @EventHandler
    // End Gateway blocks. Only gets triggered for EndGateway with an exit_portal:[X, Y, Z] attribute
    public void onPlayerTeleportEndGateway(PlayerTeleportEndGatewayEvent event) {
        Utils.initializer().getComponentLogger().error("onPlayerTeleportEndGateway");

        DungeonManager manager = Utils.configs().DUNGEON_MANAGER;

        String dimension = ((CraftWorld) event.getGateway().getWorld()).getHandle().dimension().location().toString();
        if (dimension.equals("minecraft:overworld")) {
            Location to = manager.getDungeonEntranceLocation();
            if (to == null) {
                event.setCancelled(true);
                Utils.initializer().getComponentLogger().error("Failed to TP player to Dungeon");
            } else {
                event.setTo(to);
                InventorySnapshot.takePlayerInventorySnapshot(event.getPlayer());
            }
        } else if (dimension.equals("lastlife:dungeon_dim")) {
            event.setTo(manager.getDungeonExitLocation());
        }
    }
}
