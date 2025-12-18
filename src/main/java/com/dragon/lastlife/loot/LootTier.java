package com.dragon.lastlife.loot;

import org.bukkit.NamespacedKey;

public enum LootTier {
    LOW(0,  new NamespacedKey("lastlife", "chests/dungeon_tier_low")),
    MID(1,  new NamespacedKey("lastlife", "chests/dungeon_tier_mid")),
    HIGH(2, new NamespacedKey("lastlife", "chests/dungeon_tier_high"));

    final NamespacedKey key;
    final int value;

    LootTier(int value, NamespacedKey key) {
        this.key = key;
        this.value = value;
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

    public int value(){
        return value;
    }
}