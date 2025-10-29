package com.dragon.lastlife.players;

import com.dragon.lastlife.utils.Utils;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.visitors.CollectToTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static net.minecraft.world.entity.player.Inventory.EQUIPMENT_SLOT_MAPPING;
import static net.minecraft.world.entity.player.Inventory.INVENTORY_SIZE;
import static org.bukkit.persistence.PersistentDataType.STRING;

public class InventorySnapshot {
    static public NamespacedKey INVENTORY_SNAPSHOT = new NamespacedKey(Utils.initializer(), "inventory_snapshot");

    static private final List<Item> BUCKETS = List.of(
            Items.BUCKET, Items.WATER_BUCKET, Items.LAVA_BUCKET, Items.POWDER_SNOW_BUCKET, Items.MILK_BUCKET,
            Items.AXOLOTL_BUCKET, Items.COD_BUCKET, Items.PUFFERFISH_BUCKET, Items.SALMON_BUCKET, Items.TADPOLE_BUCKET, Items.TROPICAL_FISH_BUCKET
    );
    static private final List<Item> BOTTLES = List.of(
            Items.GLASS_BOTTLE, Items.POTION, Items.HONEY_BOTTLE
    );
    static private final List<Item> BOWLS = List.of(
            Items.BOWL, Items.MUSHROOM_STEW, Items.SUSPICIOUS_STEW, Items.RABBIT_STEW, Items.BEETROOT_SOUP
    );
    static private final List<List<Item>> TRANSFORMATIVE_ITEM_TYPES = List.of(
            BUCKETS, BOTTLES, BOWLS
    );

    static private final List<Item> TRANSFORMATIVE_ITEMS = TRANSFORMATIVE_ITEM_TYPES.stream().flatMap(List::stream).toList();

    public static void takePlayerInventorySnapshot(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(serverPlayer.problemPath(), Utils.initializer().getSLF4JLogger())) {
            TagValueOutput nbt = TagValueOutput.createWithContext(scopedCollector, serverPlayer.registryAccess());
            ValueOutput.TypedOutputList<ItemStackWithSlot> snapshot = nbt.list("InventorySnapshot", ItemStackWithSlot.CODEC);

            List<ItemStack> items = serverPlayer.getInventory().getContents();

            for (int i = 0; i < items.size(); ++i) {
                ItemStack itemStack = items.get(i);
                if (!itemStack.isEmpty()) {
                    snapshot.add(new ItemStackWithSlot(i, itemStack));
                }
            }

            CompoundTag result = nbt.buildResult();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(stream);
            try {
                NbtIo.write(result, output);
                player.getPersistentDataContainer().set(INVENTORY_SNAPSHOT, STRING, stream.toString());
            } catch (IOException e) {
                Utils.initializer().getComponentLogger().error("Failed to write CompundTag: ", e);
            }
        }
    }

    private static SimpleContainer getInventorySnapshot(ServerPlayer player) {
        CraftPlayer craftPlayer = player.getBukkitEntity();
        String data = craftPlayer.getPersistentDataContainer().get(INVENTORY_SNAPSHOT, STRING);
        if (data == null) {
            return null;
        }

        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(player.problemPath(), Utils.initializer().getSLF4JLogger())) {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(data.getBytes()));
            CollectToTag tag = new CollectToTag();
            NbtIo.parse(input, tag, NbtAccounter.create(104857600L));
            SimpleContainer container = new SimpleContainer(EQUIPMENT_SLOT_MAPPING.size() + INVENTORY_SIZE);

            ValueInput tagInput = TagValueInput.createGlobal(scopedCollector, tag.getResult().asCompound().get());

            for (ItemStackWithSlot itemStackWithSlot : tagInput.list("InventorySnapshot", ItemStackWithSlot.CODEC).get()) {
                if (itemStackWithSlot.isValidInContainer(container.items.size())) {
                    container.setItem(itemStackWithSlot.slot(), itemStackWithSlot.stack());
                }
            }

            return container;
        } catch (IOException e) {
            Utils.initializer().getComponentLogger().error("Failed to parse CompundTag: ", e);
            return null;
        }
    }

    private static HashMap<Item, Integer> getTransformativeItemsCountMap(List<ItemStack> items) {
        HashMap<Item, Integer> transformativeItems = new HashMap<>();

        for (ItemStack stack : items) {
            Item item = stack.getItem();

            if (TRANSFORMATIVE_ITEMS.contains(item)) {
                int count = stack.getCount();

                transformativeItems.compute(item, (ii, value) -> value == null ? count : count + value);
            }
        }

        return transformativeItems;
    }

    private static int findMatchingItem(ItemStack item, List<ItemStack> snapshotItems, HashMap<Item, Integer> snapshotTransformativeItems) {
        for (int i = 0; i < snapshotItems.size(); i++) {
            ItemStack snapshotItem = snapshotItems.get(i);
            // Not the same item, keep searching
            if (snapshotItem.isEmpty() || !item.is(snapshotItem.getItem())) {
                continue;
            }

            DataComponentMap itemComponents = item.getComponents();
            DataComponentMap snapshotComponents = snapshotItem.getComponents();

            // If same item but less durability -> keep. If item has mending, disregard durability check
            if (itemComponents.has(DataComponents.DAMAGE) || snapshotComponents.has(DataComponents.DAMAGE)) {
                int damage = itemComponents.getOrDefault(DataComponents.DAMAGE, 0);
                int snapshotDamage = snapshotComponents.getOrDefault(DataComponents.DAMAGE, 0);
                ItemEnchantments enchants = snapshotComponents.get(DataComponents.ENCHANTMENTS);
                DataComponentMap itemComponentsCopy = DataComponentMap.builder().addAll(itemComponents).set(DataComponents.DAMAGE, null).build();
                DataComponentMap snapshotComponentsCopy = DataComponentMap.builder().addAll(snapshotComponents).set(DataComponents.DAMAGE, null).build();

                // The items have other components differences other than durability -> Not the same item
                if (!itemComponentsCopy.equals(snapshotComponentsCopy)) {
                    continue;
                }

                // Item has taken more (durability) damage, but that's the only difference. We should keep it.
                if (damage >= snapshotDamage) {
                    return i;
                }
                // Item has mending, we don't care about durability difference
                if (enchants != null && enchants.keySet().stream().anyMatch(e -> e.is(Enchantments.MENDING))) {
                    return i;
                }
            } else if (itemComponents.equals(snapshotComponents)) {
                return i;
            }
        }

        // Check if item is transformative
        Optional<List<Item>> transformativeCategory = TRANSFORMATIVE_ITEM_TYPES.stream().filter(list -> list.contains(item.getItem())).findFirst();

        if (transformativeCategory.isPresent()) {
            // find transformed item in snapshot. We ignore components for transformed items
            for (int i = 0; i < snapshotItems.size(); i++) {
                ItemStack snapshotItem = snapshotItems.get(i);
                Item itemType = snapshotItem.getItem();
                int available = snapshotTransformativeItems.getOrDefault(itemType, 0);

                if (transformativeCategory.get().contains(itemType) && available > 0) {
                    //noinspection DataFlowIssue
                    snapshotTransformativeItems.compute(itemType, (key, count) -> count - 1);
                    return i;
                }
            }
        }

        return -1;
    }

    public static void applyPlayerInventorySnapshot(ServerPlayer player, PlayerDeathEvent event) {
        SimpleContainer snapshot = getInventorySnapshot(player);

        if (snapshot == null) {
            Utils.initializer().getComponentLogger().error("Failed to get InventorySnapshot - leaving player inventory untouched");
            return;
        }

        Inventory inventory = player.getInventory();
        List<ItemStack> items = inventory.getContents();
        List<ItemStack> snapshotItems = snapshot.getContents();
        @NotNull List<org.bukkit.inventory.ItemStack> drops = event.getDrops();
        HashMap<Item, Integer> transformativeItems = getTransformativeItemsCountMap(items);
        HashMap<Item, Integer> snapshotTransformativeItems = getTransformativeItemsCountMap(snapshotItems);

        // Allocate each transformative item in the snapshot to un-transformed items in inventory.
        // snapshotTransformativeItems will contain the remainder after all exact matches have been accounted for.
        snapshotTransformativeItems.replaceAll((item, count) -> {
            int newCount = transformativeItems.getOrDefault(item, 0);

            if (newCount > count) {
                return 0;
            } else {
                return count - newCount;
            }
        });

        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            if (item.isEmpty()) {
                continue;
            }

            int currentCount = item.getCount();

            // In case player merged 2 item stacks, we will loop until we find all the quantities in the snapshot, or run out of matching items
            while (true) {
                int snapshotItemIndex = findMatchingItem(item, snapshotItems, snapshotTransformativeItems);

                // Item not found in snapshot : dropping
                if (snapshotItemIndex == -1) {
                    int countFound = item.getCount() - currentCount;

                    if (countFound == 0) {
                        // Item was completely not found in snapshot - removing from inventory
                        drops.add(item.asBukkitCopy());
                        inventory.setItem(i, ItemStack.EMPTY);
                    } else {
                        // Item was partially found in snapshot - keeping that amount and dropping the excess
                        org.bukkit.inventory.ItemStack drop = item.asBukkitCopy();

                        item.setCount(countFound);
                        drop.subtract(countFound);
                        drops.add(drop);
                    }
                    break;
                }

                ItemStack snapshotItem = snapshotItems.get(snapshotItemIndex);

                int countBefore = snapshotItem.getCount();
                int countDifference = currentCount - countBefore;

                // player has less quantity than in snapshot
                if (countDifference < 0) {
                    // In case the player split the stack, we keep track of how many where used
                    snapshotItem.shrink(currentCount);
                    break;
                }

                // SnapshotItem quantity has been fully "used", removing it from snapshot so it's not used by subsequent item searches
                snapshotItems.set(snapshotItemIndex, ItemStack.EMPTY);

                // Got exact count match, continue to next item
                if (countDifference == 0) {
                    break;
                }

                // If item count higher -> Count we found some, and keep looking
                currentCount -= countBefore;
            }
        }
    }
}
