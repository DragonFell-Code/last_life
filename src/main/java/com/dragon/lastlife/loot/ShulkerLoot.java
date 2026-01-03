package com.dragon.lastlife.loot;

import org.bukkit.Material;
import org.bukkit.MusicInstrument;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.MusicInstrumentMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.List;

public class ShulkerLoot {
    static final List<ItemStack[]> SHULKER_CONTENTS_LIST;

    static {
        ItemStack EMPTY = ItemStack.of(Material.AIR);
        ItemStack REDSTONE_ORE = ItemStack.of(Material.REDSTONE_ORE, 16);
        ItemStack DEEPSLATE_REDSTONE_ORE = ItemStack.of(Material.DEEPSLATE_REDSTONE_ORE, 16);
        ItemStack DAYLIGHT_DETECTOR = ItemStack.of(Material.DAYLIGHT_DETECTOR);
        ItemStack JUKEBOX = ItemStack.of(Material.JUKEBOX);
        ItemStack BAMBOO = ItemStack.of(Material.BAMBOO, 64);
        ItemStack CRAFTER = ItemStack.of(Material.CRAFTER);
        ItemStack OBSERVER = ItemStack.of(Material.OBSERVER, 16);
        ItemStack[] shulker1 = {
                EMPTY, EMPTY, EMPTY, REDSTONE_ORE, DAYLIGHT_DETECTOR, DEEPSLATE_REDSTONE_ORE, EMPTY, EMPTY, EMPTY,
                EMPTY, EMPTY, EMPTY, JUKEBOX, BAMBOO, CRAFTER, EMPTY, EMPTY, EMPTY,
                EMPTY, EMPTY, EMPTY, DEEPSLATE_REDSTONE_ORE, OBSERVER, REDSTONE_ORE, EMPTY, EMPTY, EMPTY
        };

        ItemStack POTATO = ItemStack.of(Material.POTATO, 64);
        ItemStack CARROT = ItemStack.of(Material.CARROT, 64);
        ItemStack BEETROOT_SEEDS = ItemStack.of(Material.BEETROOT_SEEDS, 16);
        ItemStack WHEAT_SEEDS = ItemStack.of(Material.WHEAT_SEEDS, 32);
        ItemStack MELON_SEEDS = ItemStack.of(Material.MELON_SEEDS, 32);
        ItemStack PUMPKIN_SEEDS = ItemStack.of(Material.PUMPKIN_SEEDS, 32);
        ItemStack CHICKEN_SPAWN_EGG = ItemStack.of(Material.CHICKEN_SPAWN_EGG, 1);
        ItemStack SHEEP_SPAWN_EGG = ItemStack.of(Material.SHEEP_SPAWN_EGG, 1);
        ItemStack SPAWNER = ItemStack.of(Material.SPAWNER, 1);
        ItemStack[] shulker2 = {
                EMPTY, EMPTY, EMPTY, POTATO, BEETROOT_SEEDS, POTATO, EMPTY, EMPTY, EMPTY,
                EMPTY, EMPTY, SPAWNER, MELON_SEEDS, WHEAT_SEEDS, PUMPKIN_SEEDS, SPAWNER, EMPTY, EMPTY,
                EMPTY, EMPTY, SHEEP_SPAWN_EGG, CARROT, BEETROOT_SEEDS, CARROT, CHICKEN_SPAWN_EGG, EMPTY, EMPTY
        };

        ItemStack SPRUCE_SAPLING = ItemStack.of(Material.SPRUCE_SAPLING, 16);
        ItemStack BAMBOO16 = ItemStack.of(Material.BAMBOO, 16);
        ItemStack CHERRY_SAPLING = ItemStack.of(Material.CHERRY_SAPLING, 16);
        ItemStack DARK_OAK_SAPLING = ItemStack.of(Material.DARK_OAK_SAPLING, 32);
        ItemStack JUNGLE_SAPLING = ItemStack.of(Material.JUNGLE_SAPLING, 16);
        ItemStack MANGROVE_PROPAGULE = ItemStack.of(Material.MANGROVE_PROPAGULE, 16);
        ItemStack OAK_SAPLING = ItemStack.of(Material.OAK_SAPLING, 16);
        ItemStack BONE_BLOCK = ItemStack.of(Material.BONE_BLOCK, 16);
        ItemStack BIRCH_SAPLING = ItemStack.of(Material.BIRCH_SAPLING, 16);
        ItemStack ACACIA_SAPLING = ItemStack.of(Material.ACACIA_SAPLING, 16);
        ItemStack[] shulker3 = {
                EMPTY, SPRUCE_SAPLING, BAMBOO16, CHERRY_SAPLING, DARK_OAK_SAPLING, CHERRY_SAPLING, BAMBOO16, SPRUCE_SAPLING, EMPTY,
                EMPTY, JUNGLE_SAPLING, MANGROVE_PROPAGULE, OAK_SAPLING, BONE_BLOCK, OAK_SAPLING, MANGROVE_PROPAGULE, JUNGLE_SAPLING, EMPTY,
                EMPTY, BIRCH_SAPLING, BAMBOO16, ACACIA_SAPLING, DARK_OAK_SAPLING, ACACIA_SAPLING, BAMBOO16, BIRCH_SAPLING, EMPTY
        };

        ItemStack DEEPSLATE_DIAMOND_ORE = ItemStack.of(Material.DEEPSLATE_DIAMOND_ORE, 4);
        ItemStack SADDLE = ItemStack.of(Material.SADDLE, 1);
        ItemStack DIAMOND_HORSE_ARMOR = ItemStack.of(Material.DIAMOND_HORSE_ARMOR, 1);
        ItemStack SKELETON_HORSE_SPAWN_EGG = ItemStack.of(Material.SKELETON_HORSE_SPAWN_EGG, 1);
        ItemStack[] shulker4 = {
                DEEPSLATE_DIAMOND_ORE, EMPTY, DEEPSLATE_DIAMOND_ORE, SADDLE, SADDLE, SADDLE, DEEPSLATE_DIAMOND_ORE, EMPTY, DEEPSLATE_DIAMOND_ORE,
                EMPTY, EMPTY, EMPTY, DIAMOND_HORSE_ARMOR, DIAMOND_HORSE_ARMOR, DIAMOND_HORSE_ARMOR, EMPTY, EMPTY, EMPTY,
                DEEPSLATE_DIAMOND_ORE, EMPTY, DEEPSLATE_DIAMOND_ORE, SKELETON_HORSE_SPAWN_EGG, SKELETON_HORSE_SPAWN_EGG, SKELETON_HORSE_SPAWN_EGG, DEEPSLATE_DIAMOND_ORE, EMPTY, DEEPSLATE_DIAMOND_ORE
        };

        ItemStack OBSIDIAN = ItemStack.of(Material.OBSIDIAN, 1);
        ItemStack ALLAY_SPAWN_EGG = ItemStack.of(Material.ALLAY_SPAWN_EGG, 1);
        ItemStack EXPERIENCE_BOTTLE = ItemStack.of(Material.EXPERIENCE_BOTTLE, 64);
        ItemStack WITCH_SPAWN_EGG = ItemStack.of(Material.WITCH_SPAWN_EGG, 1);
        ItemStack BOOK = ItemStack.of(Material.BOOK, 32);
        ItemStack LAPIS_ORE1 = ItemStack.of(Material.LAPIS_ORE, 1);
        ItemStack LAPIS_ORE21 = ItemStack.of(Material.LAPIS_ORE, 21);
        ItemStack[] shulker5 = {
                OBSIDIAN, EMPTY, EMPTY, ALLAY_SPAWN_EGG, EXPERIENCE_BOTTLE, WITCH_SPAWN_EGG, EMPTY, EMPTY, OBSIDIAN,
                EMPTY, EMPTY, EMPTY, BOOK, LAPIS_ORE1, BOOK, EMPTY, EMPTY, EMPTY,
                OBSIDIAN, EMPTY, EMPTY, LAPIS_ORE21, LAPIS_ORE21, LAPIS_ORE21, EMPTY, EMPTY, OBSIDIAN
        };

        ItemStack PILLAGER_SPAWN_EGG = ItemStack.of(Material.PILLAGER_SPAWN_EGG, 1);
        ItemStack EVOKER_SPAWN_EGG = ItemStack.of(Material.EVOKER_SPAWN_EGG, 1);
        ItemStack VINDICATOR_SPAWN_EGG = ItemStack.of(Material.VINDICATOR_SPAWN_EGG, 1);
        ItemStack EMERALD_ORE = ItemStack.of(Material.EMERALD_ORE, 8);
        ItemStack[] shulker6 = {
                EMPTY, EMPTY, EMPTY, PILLAGER_SPAWN_EGG, EVOKER_SPAWN_EGG, VINDICATOR_SPAWN_EGG, EMPTY, EMPTY, EMPTY,
                EMPTY, EMPTY, EMERALD_ORE, SPAWNER, EMPTY, SPAWNER, EMERALD_ORE, EMPTY, EMPTY,
                EMPTY, EMPTY, EMPTY, EMERALD_ORE, EMERALD_ORE, EMERALD_ORE, EMPTY, EMPTY, EMPTY
        };

        ItemStack LURE3 = ItemStack.of(Material.ENCHANTED_BOOK, 1);
        ItemStack WATER_BUCKET = ItemStack.of(Material.WATER_BUCKET, 1);
        ItemStack FISHING_ROD = ItemStack.of(Material.FISHING_ROD, 1);
        ItemStack LUCK_OF_THE_SEA3 = ItemStack.of(Material.ENCHANTED_BOOK, 1);
        ItemStack COD_SPAWN_EGG = ItemStack.of(Material.COD_SPAWN_EGG, 1);
        ItemStack SALMON_SPAWN_EGG = ItemStack.of(Material.SALMON_SPAWN_EGG, 1);
        EnchantmentStorageMeta lure_meta = (EnchantmentStorageMeta)LURE3.getItemMeta();
        EnchantmentStorageMeta luck_of_the_sea_meta = (EnchantmentStorageMeta)LUCK_OF_THE_SEA3.getItemMeta();
        lure_meta.addStoredEnchant(Enchantment.LURE, 3, false);
        luck_of_the_sea_meta.addStoredEnchant(Enchantment.LUCK_OF_THE_SEA, 3, false);
        LURE3.setItemMeta(lure_meta);
        LUCK_OF_THE_SEA3.setItemMeta(luck_of_the_sea_meta);
        ItemStack[] shulker7 = {
                EMPTY, LURE3, EMPTY, WATER_BUCKET, FISHING_ROD, WATER_BUCKET, EMPTY, LUCK_OF_THE_SEA3, EMPTY,
                EMPTY, LURE3, EMPTY, SPAWNER, FISHING_ROD, SPAWNER, EMPTY, LUCK_OF_THE_SEA3, EMPTY,
                EMPTY, LURE3, EMPTY, COD_SPAWN_EGG, FISHING_ROD, SALMON_SPAWN_EGG, EMPTY, LUCK_OF_THE_SEA3, EMPTY
        };

        ItemStack END_CRYSTAL = ItemStack.of(Material.END_CRYSTAL, 1);
        ItemStack GOAT_HORN_SEEK = ItemStack.of(Material.GOAT_HORN, 1);
        ItemStack IRON_AXE = ItemStack.of(Material.IRON_AXE, 1);
        ItemStack GOAT_HORN_PONDER = ItemStack.of(Material.GOAT_HORN, 1);
        ItemStack CHAINMAIL_CHESTPLATE = ItemStack.of(Material.CHAINMAIL_CHESTPLATE, 1);
        ItemStack COBWEB = ItemStack.of(Material.COBWEB, 32);
        ItemStack SHIELD = ItemStack.of(Material.SHIELD, 1);
        MusicInstrumentMeta ponderMeta = ((MusicInstrumentMeta)GOAT_HORN_PONDER.getItemMeta());
        MusicInstrumentMeta seekMeta = ((MusicInstrumentMeta)GOAT_HORN_SEEK.getItemMeta());
        ponderMeta.setInstrument(MusicInstrument.PONDER_GOAT_HORN);
        seekMeta.setInstrument(MusicInstrument.SEEK_GOAT_HORN);
        GOAT_HORN_PONDER.setItemMeta(ponderMeta);
        GOAT_HORN_SEEK.setItemMeta(seekMeta);
        ItemStack[] shulker8 = {
                END_CRYSTAL, GOAT_HORN_SEEK, END_CRYSTAL, IRON_AXE, IRON_AXE, IRON_AXE, END_CRYSTAL, GOAT_HORN_PONDER, END_CRYSTAL,
                END_CRYSTAL, EMPTY, END_CRYSTAL, CHAINMAIL_CHESTPLATE, CHAINMAIL_CHESTPLATE, CHAINMAIL_CHESTPLATE, END_CRYSTAL, EMPTY, END_CRYSTAL,
                END_CRYSTAL, COBWEB, END_CRYSTAL, SHIELD, SHIELD, SHIELD, END_CRYSTAL, COBWEB, END_CRYSTAL
        };

        ItemStack HEALING_ARROW = ItemStack.of(Material.TIPPED_ARROW, 3);
        ItemStack WIND_CHARGE_ARROW = ItemStack.of(Material.TIPPED_ARROW, 3);
        ItemStack ARROW = ItemStack.of(Material.ARROW, 32);
        ItemStack OOZING_ARROW = ItemStack.of(Material.TIPPED_ARROW, 3);
        ItemStack SWIFTNESS_ARROW = ItemStack.of(Material.TIPPED_ARROW, 3);
        ItemStack CROSSBOW = ItemStack.of(Material.CROSSBOW, 1);
        ItemStack WEAKNESS_ARROW = ItemStack.of(Material.TIPPED_ARROW, 3);
        ItemStack WEAVING_ARROW = ItemStack.of(Material.TIPPED_ARROW, 3);
        ItemStack BOW = ItemStack.of(Material.BOW, 1);
        PotionMeta healingMeta = (PotionMeta) HEALING_ARROW.getItemMeta();
        PotionMeta wind_chargeMeta = (PotionMeta) WIND_CHARGE_ARROW.getItemMeta();
        PotionMeta oozingMeta = (PotionMeta) OOZING_ARROW.getItemMeta();
        PotionMeta swiftnessMeta = (PotionMeta) SWIFTNESS_ARROW.getItemMeta();
        PotionMeta weaknessMeta = (PotionMeta) WEAKNESS_ARROW.getItemMeta();
        PotionMeta weavingMeta = (PotionMeta) WEAVING_ARROW.getItemMeta();
        healingMeta.setBasePotionType(PotionType.STRONG_HEALING);
        wind_chargeMeta.setBasePotionType(PotionType.WIND_CHARGED);
        oozingMeta.setBasePotionType(PotionType.OOZING);
        swiftnessMeta.setBasePotionType(PotionType.LONG_SWIFTNESS);
        weaknessMeta.setBasePotionType(PotionType.LONG_WEAKNESS);
        weavingMeta.setBasePotionType(PotionType.WEAVING);
        HEALING_ARROW.setItemMeta(healingMeta);
        WIND_CHARGE_ARROW.setItemMeta(wind_chargeMeta);
        OOZING_ARROW.setItemMeta(oozingMeta);
        SWIFTNESS_ARROW.setItemMeta(swiftnessMeta);
        WEAKNESS_ARROW.setItemMeta(weaknessMeta);
        WEAVING_ARROW.setItemMeta(weavingMeta);
        ItemStack[] shulker9 = {
                HEALING_ARROW, EMPTY, WIND_CHARGE_ARROW, ARROW, EMPTY, ARROW, WIND_CHARGE_ARROW, EMPTY, HEALING_ARROW,
                EMPTY, EMPTY, OOZING_ARROW, SWIFTNESS_ARROW, CROSSBOW, SWIFTNESS_ARROW, OOZING_ARROW, EMPTY, EMPTY,
                WEAKNESS_ARROW, EMPTY, WEAVING_ARROW, BOW, EMPTY, BOW, WEAVING_ARROW, EMPTY, WEAKNESS_ARROW
        };

        ItemStack COOKED_PORKCHOP = ItemStack.of(Material.COOKED_PORKCHOP, 64);
        ItemStack GOLDEN_CARROT = ItemStack.of(Material.GOLDEN_CARROT, 32);
        ItemStack COOKED_BEEF = ItemStack.of(Material.COOKED_BEEF, 64);
        ItemStack CAKE = ItemStack.of(Material.CAKE, 1);
        ItemStack COW_SPAWN_EGG = ItemStack.of(Material.COW_SPAWN_EGG, 1);
        ItemStack COOKED_MUTTON = ItemStack.of(Material.COOKED_MUTTON, 64);
        ItemStack PIG_SPAWN_EGG = ItemStack.of(Material.PIG_SPAWN_EGG, 1);
        ItemStack[] shulker10 = {
                EMPTY, EMPTY, EMPTY, COOKED_PORKCHOP, GOLDEN_CARROT, COOKED_PORKCHOP, EMPTY, EMPTY, EMPTY,
                EMPTY, EMPTY, SPAWNER, COOKED_BEEF, CAKE, COOKED_BEEF, SPAWNER, EMPTY, EMPTY,
                EMPTY, EMPTY, COW_SPAWN_EGG, COOKED_MUTTON, GOLDEN_CARROT, COOKED_MUTTON, PIG_SPAWN_EGG, EMPTY, EMPTY
        };

        SHULKER_CONTENTS_LIST = List.of(shulker1, shulker2, shulker3, shulker4, shulker5, shulker6, shulker7, shulker8, shulker9, shulker10);
    }
}
