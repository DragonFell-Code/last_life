package com.dragon.lastlife.loot;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

import java.util.List;

public class BundleLoot {
    public static final List<ItemStack> BUNDLE_LIST;

    static {
        ItemStack trading_bundle = new ItemStack(Material.LIGHT_BLUE_BUNDLE);
        ItemStack functional_bundle = new ItemStack(Material.RED_BUNDLE);
        ItemStack food_bundle = new ItemStack(Material.YELLOW_BUNDLE);
        ItemStack farmer_bundle = new ItemStack(Material.BROWN_BUNDLE);
        ItemStack lumberjack_bundle = new ItemStack(Material.GREEN_BUNDLE);
        ItemStack kitchen_sink = new ItemStack(Material.PURPLE_BUNDLE);

        BundleMeta tradingMeta = (BundleMeta)trading_bundle.getItemMeta();
        tradingMeta.addItem(new ItemStack(Material.IRON_BLOCK, 16));
        tradingMeta.addItem(new ItemStack(Material.EMERALD_BLOCK, 16));
        tradingMeta.addItem(new ItemStack(Material.IRON_INGOT, 16));
        tradingMeta.addItem(new ItemStack(Material.EMERALD, 16));

        tradingMeta.displayName(Component.text("Trading Bundle").color(NamedTextColor.DARK_AQUA));
        trading_bundle.setItemMeta(tradingMeta);

        BundleMeta functionalMeta = (BundleMeta)functional_bundle.getItemMeta();
        functionalMeta.addItem(new ItemStack(Material.COAL_BLOCK, 8));
        functionalMeta.addItem(new ItemStack(Material.REDSTONE_BLOCK, 8));
        functionalMeta.addItem(new ItemStack(Material.QUARTZ, 32));
        functionalMeta.addItem(new ItemStack(Material.STICK, 16));

        functionalMeta.displayName(Component.text("Functional Bundle").color(NamedTextColor.RED));
        functional_bundle.setItemMeta(functionalMeta);

        BundleMeta foodMeta = (BundleMeta)food_bundle.getItemMeta();
        foodMeta.addItem(new ItemStack(Material.GOLDEN_APPLE, 4));
        foodMeta.addItem(new ItemStack(Material.GOLDEN_CARROT, 28));
        foodMeta.addItem(new ItemStack(Material.COOKED_BEEF, 32));

        foodMeta.displayName(Component.text("Food Bundle").color(NamedTextColor.YELLOW));
        food_bundle.setItemMeta(foodMeta);

        BundleMeta farmerMeta = (BundleMeta)farmer_bundle.getItemMeta();
        farmerMeta.addItem(new ItemStack(Material.WHEAT_SEEDS, 8));
        farmerMeta.addItem(new ItemStack(Material.PUMPKIN, 8));
        farmerMeta.addItem(new ItemStack(Material.MELON, 8));
        farmerMeta.addItem(new ItemStack(Material.CARROT, 8));
        farmerMeta.addItem(new ItemStack(Material.POTATO, 8));
        farmerMeta.addItem(new ItemStack(Material.BEETROOT_SEEDS, 8));
        farmerMeta.addItem(new ItemStack(Material.BONE_BLOCK, 16));

        farmerMeta.displayName(Component.text("Farmer Bundle").color(TextColor.color(205,133,63)));
        farmer_bundle.setItemMeta(farmerMeta);

        BundleMeta lumberjackMeta = (BundleMeta)lumberjack_bundle.getItemMeta();
        lumberjackMeta.addItem(new ItemStack(Material.OAK_SAPLING, 8));
        lumberjackMeta.addItem(new ItemStack(Material.SPRUCE_SAPLING, 8));
        lumberjackMeta.addItem(new ItemStack(Material.ACACIA_SAPLING, 8));
        lumberjackMeta.addItem(new ItemStack(Material.DARK_OAK_SAPLING, 8));
        lumberjackMeta.addItem(new ItemStack(Material.JUNGLE_SAPLING, 8));
        lumberjackMeta.addItem(new ItemStack(Material.CHERRY_SAPLING, 8));
        lumberjackMeta.addItem(new ItemStack(Material.BIRCH_SAPLING, 8));
        lumberjackMeta.addItem(new ItemStack(Material.PALE_OAK_SAPLING, 8));

        lumberjackMeta.displayName(Component.text("Lumberjack Bundle").color(NamedTextColor.DARK_GREEN));
        lumberjack_bundle.setItemMeta(lumberjackMeta);

        BundleMeta kitchenMeta = (BundleMeta)kitchen_sink.getItemMeta();
        kitchenMeta.addItem(new ItemStack(Material.OAK_SAPLING, 8)); // TODO: Would be nice to make the sapling type random
        kitchenMeta.addItem(new ItemStack(Material.PUMPKIN, 4));
        kitchenMeta.addItem(new ItemStack(Material.CARROT, 4));
        kitchenMeta.addItem(new ItemStack(Material.COAL, 8));
        kitchenMeta.addItem(new ItemStack(Material.STICK, 8));
        kitchenMeta.addItem(new ItemStack(Material.EMERALD_BLOCK, 8));
        kitchenMeta.addItem(new ItemStack(Material.IRON_BLOCK, 8));
        kitchenMeta.addItem(new ItemStack(Material.REDSTONE_BLOCK, 8));
        kitchenMeta.addItem(new ItemStack(Material.GOLDEN_APPLE, 8));

        kitchenMeta.displayName(Component.text("Kitchen Sink").color(NamedTextColor.DARK_PURPLE));
        kitchen_sink.setItemMeta(kitchenMeta);

        BUNDLE_LIST = List.of(trading_bundle, functional_bundle, food_bundle, farmer_bundle, lumberjack_bundle, kitchen_sink);
    }
}
