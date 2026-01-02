package com.dragon.lastlife.listeners;

import com.dragon.lastlife.loot.LootManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;

import java.util.Collection;
import java.util.Random;

public class LootListener implements Listener {
    public static int MAX_DONATION_LEVEL = 9;

    public static Integer donation_level_override = null;

    @EventHandler(ignoreCancelled = true)
    public void onLoot(LootGenerateEvent event) {
        NamespacedKey originalKey = event.getLootTable().getKey();
        // Only target our dungeon chests
        if (!originalKey.getNamespace().equals("lastlife") || !originalKey.getKey().equals("chests/dungeon_scaled")) return;

        int level = getDonationLevel();
        NamespacedKey key = new NamespacedKey("lastlife", "chests/dungeon_tier_" + level);
        LootTable table = Bukkit.getLootTable(key);

        if (table != null) {
            // Generate items from the tier table into this event
            Collection<ItemStack> generated = table.populateLoot(new Random(), event.getLootContext());
            event.getLoot().clear();
            event.getLoot().addAll(generated);
        }
    }

    public static int getDonationLevel() {
        // Determine the tier from donations
        int level = LootManager.getDonationLevel();

        if (donation_level_override != null) {
            level = donation_level_override;
        }
        return Math.clamp(level, 1, MAX_DONATION_LEVEL);
    }

    public static void setDonationLevelOverride(Integer value) {
        if (value == null) {
            donation_level_override = null;
        } else {
            donation_level_override = Math.clamp(value, 1, MAX_DONATION_LEVEL);
        }
    }
}
