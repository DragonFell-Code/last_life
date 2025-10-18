package com.dragon.lastlife.listeners;

import com.dragon.lastlife.config.DonationConfig;
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
        System.out.println("Loot generated!");
        NamespacedKey key = event.getLootTable().getKey();
        // Only target our dungeon chests
        if (!key.getNamespace().equals("lastlife") || !key.getKey().equals("chests/dungeon_scaled")) return;

        // Determine the tier from donations
        DonationConfig config = Utils.configs().DONATION_CONFIG();
        Tier tier = Tier.of(config.total.doubleValue());

        LootTable table = Bukkit.getLootTable(tier.key());
        if (table != null) {
            // Generate items from the tier table into this event
            Collection<ItemStack> generated = table.populateLoot(new Random(), event.getLootContext());
            event.getLoot().clear();
            event.getLoot().addAll(generated);
        }
    }

    enum Tier {
        LOW(new NamespacedKey("lastlife", "chests/dungeon_tier_low")),
        MID(new NamespacedKey("lastlife", "chests/dungeon_tier_mid")),
        HIGH(new NamespacedKey("lastlife", "chests/dungeon_tier_high"));

        final NamespacedKey key;

        Tier(NamespacedKey key) {
            this.key = key;
        }

        public static Tier of(double amount) {
            if (amount < 1000) return LOW;
            if (amount < 10000) return MID;
            return HIGH;
        }

        public static Tier of(NamespacedKey key) {
            for (Tier tier : values()) {
                if (tier.key().equals(key)) return tier;
            }
            return null;
        }

        public NamespacedKey key() {
            return key;
        }
    }
}
