package com.dragon.lastlife.loot;

import com.dragon.lastlife.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Random;

public class LootManager {
    private static final Random random = new Random();
    private static final int LOBBY_1_TEAM_ID = 73169;
    private static final int LOBBY_2_TEAM_ID = 73168;

    public static Collection<ItemStack> generate(LootTier tier, Location location){
        LootTable table = table(tier);
        if(table == null) return null;
        return table.populateLoot(random, new LootContext.Builder(location).build());
    }

    public static String color(LootTier tier){
        return DyeColor.values()[tier.value()+1 % DyeColor.values().length].name();

    }

    public static int getLobbyID() {
        int team_id = Utils.configs().DONATION_CONFIG().team_id;

        if (team_id == LOBBY_1_TEAM_ID) {
            return 1;
        } else if (team_id == LOBBY_2_TEAM_ID) {
            return 2;
        }
        throw new RuntimeException("Invalid Team ID");
    }

    public static int getDonationLevel() {
        BigDecimal increments = BigDecimal.valueOf(getLobbyID() == 1 ? 1000 : 500);

        return Utils.configs().DONATION_CONFIG().total.divide(increments, RoundingMode.DOWN).intValue();
    }

    public static @Nullable LootTable table(LootTier tier) {
        return Bukkit.getLootTable(tier.key());
    }
}
