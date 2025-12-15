package com.dragon.lastlife.loot.delivery;

import com.dragon.lastlife.nms.CustomFox;
import com.dragon.lastlife.nms.NmsEntityFactory;
import com.dragon.lastlife.party.Party;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.block.ShulkerBox;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;

import java.lang.reflect.InvocationTargetException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class FoxDelivery extends DeliverySystem{

    private final LootTable table;
    private final Location mailbox;

    public FoxDelivery(Party party, LootTable table) {
        mailbox = party.mailbox().clone().add(0,1,0);
        this.table = table;
    }

    @Override
    void tick() {
        int radius = 30;
        // TODO: Randomize it ?
        Location spawn = mailbox.clone().add(
                new Random().nextInt(radius) * (new Random().nextBoolean() ? 1 : -1),
                new Random().nextInt(radius)* (new Random().nextBoolean() ? 1 : -1),
                new Random().nextInt(radius)* (new Random().nextBoolean() ? 1 : -1));
        while(!spawn.getBlock().getType().isSolid()){
            spawn = mailbox.clone().add(
                    new Random().nextInt(radius) * (new Random().nextBoolean() ? 1 : -1),
                    new Random().nextInt(radius)* (new Random().nextBoolean() ? 1 : -1),
                    new Random().nextInt(radius)* (new Random().nextBoolean() ? 1 : -1));
        }
        while (spawn.getBlock().getType().isSolid()) {
            // TODO: Also check for valid spawn ? (eg: not in a wall)
            // Does MC already has methods to locate a safe-spawn position ?
            spawn.add(0, 1, 0);
        }

        // Set a target – here we use the player's current location,
        // but this can be any arbitrary Location you pass in
        Location target = mailbox.clone().add(0,1,0);

        try {
            // Spawn our custom NMS fox with overridden AI that walks to target
            CustomFox fox = NmsEntityFactory.spawn(spawn, CustomFox.class);
            Vec3 vecTarget = new Vec3(target.x(), target.y(), target.z());
            fox.deliverTo(vecTarget);
            // Build a Bukkit shulker box item with a loot table, then convert to NMS
            org.bukkit.inventory.ItemStack bukkitShulker = new org.bukkit.inventory.ItemStack(org.bukkit.Material.SHULKER_BOX);
            if (table != null) {
                BlockStateMeta meta = (BlockStateMeta) bukkitShulker.getItemMeta();
                if (meta != null && meta.getBlockState() instanceof ShulkerBox shulkerState) {
                    // Set the loot table so that when placed, contents are generated like vanilla
                    if (shulkerState instanceof Lootable lootable) {
                        lootable.setLootTable(table);
                        lootable.setSeed(ThreadLocalRandom.current().nextLong());
                    }
                    shulkerState.update();
                    meta.setBlockState(shulkerState);
                    bukkitShulker.setItemMeta(meta);
                }
            }
            // Convert to NMS for the fox to hold
            ItemStack nmsItem = CraftItemStack.asNMSCopy(bukkitShulker);
            fox.setItemSlot(EquipmentSlot.MAINHAND, nmsItem);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        stop();
    }
}
