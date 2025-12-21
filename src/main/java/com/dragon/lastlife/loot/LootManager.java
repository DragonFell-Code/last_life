package com.dragon.lastlife.loot;

import com.dragon.lastlife.nms.CustomFox;
import com.dragon.lastlife.nms.NmsEntityFactory;
import com.dragon.lastlife.players.Participant;
import com.dragon.lastlife.utils.Utils;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Random;

public class LootManager {

    private static final Random random = new Random();

    public static Collection<ItemStack> generate(LootTier tier, Location location){
        LootTable table = table(tier);
        if(table == null) return null;
        return table.populateLoot(random, new LootContext.Builder(location).build());
    }

    public static String color(LootTier tier){
        return DyeColor.values()[tier.value()+1 % DyeColor.values().length].name();

    }

    public static @Nullable LootTable table(LootTier tier) {
        return Bukkit.getLootTable(tier.key());
    }
}
