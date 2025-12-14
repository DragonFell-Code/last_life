package com.dragon.lastlife.loot;

import org.bukkit.NamespacedKey;

public enum LootTier {
    LOW(new NamespacedKey("lastlife", "chests/dungeon_tier_low")),
    MID(new NamespacedKey("lastlife", "chests/dungeon_tier_mid")),
    HIGH(new NamespacedKey("lastlife", "chests/dungeon_tier_high"));

    final NamespacedKey key;

    LootTier(NamespacedKey key) {
        this.key = key;
    }

    public static LootTier of(double amount) {
        if (amount < 1000) return LOW;
        if (amount < 10000) return MID;
        return HIGH;
    }

    public static LootTier of(NamespacedKey key) {
        for (LootTier tier : values()) {
            if (tier.key().equals(key)) return tier;
        }
        return null;
    }

    public NamespacedKey key() {
        return key;
    }
}