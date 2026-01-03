package com.dragon.lastlife.loot;

import com.dragon.lastlife.players.Participant;
import com.dragon.lastlife.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static com.dragon.lastlife.loot.BundleLoot.BUNDLE_LIST;

public class LootManager {
    public static final Random random = new Random();
    private static final int LOBBY_1_TEAM_ID = 73169;
    private static final int LOBBY_2_TEAM_ID = 73168;

    public static void generateShulker(Location location) {
        DyeColor[] colors = DyeColor.values();
        DyeColor color = colors[random.nextInt(colors.length)];

        location.getBlock().setType(Material.valueOf( color + "_SHULKER_BOX"));
        ShulkerBox shulkerBoxBlock = (ShulkerBox) location.getBlock().getState();
        ItemStack[] originalContent = ShulkerLoot.SHULKER_CONTENTS_LIST.get(random.nextInt(ShulkerLoot.SHULKER_CONTENTS_LIST.size()));
        ItemStack[] contents = Arrays.stream(originalContent).map(ItemStack::clone).toArray(ItemStack[]::new);

        shulkerBoxBlock.getInventory().setContents(contents);
    }

    public static ItemStack generateBundle(Participant participant) {
        ItemStack bundle = BUNDLE_LIST.get(random.nextInt(BUNDLE_LIST.size())).clone();

        bundle.lore(List.of(Component.text("Donation from " + participant.player().getName() + "'s page")));
        return bundle;
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
}
