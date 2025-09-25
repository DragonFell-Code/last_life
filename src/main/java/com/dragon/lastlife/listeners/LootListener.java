package com.dragon.lastlife.listeners;

import com.dragon.lastlife.Initializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public class LootListener implements Listener {

    public LootListener(Initializer initializer) {
        initializer.getServer().getPluginManager().registerEvents(this, initializer);
    }

    @EventHandler(ignoreCancelled = true)
    public void onLoot(LootGenerateEvent event) {
        NamespacedKey key = event.getLootTable().getKey();
        // Only target our dungeon chests
        if (!key.getNamespace().equals("lastlife") || !key.getKey().equals("chests/dungeon_scaled")) return;

        // Determine the tier from donations
        long donations = 10; // implement this for your plugin
        Tier tier = mapDonationsToTier(donations);

        // Option A: replace with a different loot table
        NamespacedKey tierKey = switch (tier) {
            case LOW -> new NamespacedKey("lastlife", "chests/dungeon_tier_low");
            case MID -> new NamespacedKey("lastlife", "chests/dungeon_tier_mid");
            case HIGH -> new NamespacedKey("lastlife", "chests/dungeon_tier_high");
        };

        LootTable table = Bukkit.getLootTable(tierKey);
        if (table != null) {
            // Generate items from the tier table into this event
            Collection<ItemStack> generated = table.populateLoot(new Random(), event.getLootContext());
            event.getLoot().clear();
            event.getLoot().addAll(generated);
            return;
        }

        // Option B: manual injection if you want full control
        event.getLoot().clear();
        event.getLoot().add(new ItemStack(Material.DIAMOND, 3));
    }

    // Helper: donation tiers (example)
    private Tier mapDonationsToTier(long amount) {
        if (amount < 1000) return Tier.LOW; // <1k–1k+
        if (amount < 10000) return Tier.MID; // 1k–10k
        return Tier.HIGH; // 10k+
    }

    enum Tier {LOW, MID, HIGH}


}
