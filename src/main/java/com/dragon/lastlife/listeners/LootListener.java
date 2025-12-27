package com.dragon.lastlife.listeners;

import com.dragon.lastlife.config.DonationConfig;
import com.dragon.lastlife.loot.LootManager;
import com.dragon.lastlife.loot.LootTier;
import com.dragon.lastlife.utils.Utils;
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
    @EventHandler(ignoreCancelled = true)
    public void onLoot(LootGenerateEvent event) {
        NamespacedKey key = event.getLootTable().getKey();
        // Only target our dungeon chests
        if (!key.getNamespace().equals("lastlife") || !key.getKey().equals("chests/dungeon_scaled")) return;

        // Determine the tier from donations
        DonationConfig config = Utils.configs().DONATION_CONFIG();
        LootTier tier = LootTier.of(config.total.doubleValue());

        LootTable table = LootManager.table(tier);
        if (table != null) {
            // Generate items from the tier table into this event
            Collection<ItemStack> generated = table.populateLoot(new Random(), event.getLootContext());
            event.getLoot().clear();
            event.getLoot().addAll(generated);
        }
    }
}
