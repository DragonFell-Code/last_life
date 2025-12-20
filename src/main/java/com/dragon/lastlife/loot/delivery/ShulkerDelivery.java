package com.dragon.lastlife.loot.delivery;

import com.dragon.lastlife.loot.LootTier;
import com.dragon.lastlife.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;

public class ShulkerDelivery extends DeliverySystem {

    Location location;
    LootTier tier;

    public ShulkerDelivery(Location location, LootTier tier) {
        this.location = location;
        this.tier = tier;
    }

    @Override
    void tick() {
        location.getBlock().setType(Material.valueOf(DyeColor.values()[tier.value()+1 % DyeColor.values().length] + "_SHULKER_BOX"));
        ShulkerBox shulkerBoxBlock = (ShulkerBox) location.getBlock().getState();
        shulkerBoxBlock.setLootTable(Bukkit.getLootTable(tier.key()));
        shulkerBoxBlock.update();
        Utils.initializer().integration().log("DonationFlutter", "Shulker delivery to [" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + "]");
        stop();
    }
}
