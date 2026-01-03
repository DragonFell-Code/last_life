package com.dragon.lastlife.listeners;

import com.dragon.lastlife.utils.Utils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.persistence.PersistentDataType;

import static com.dragon.lastlife.world.DungeonManager.KEY_DUNGEON_CHEST_LAST_DONATION_TOTAL;
import static com.dragon.lastlife.world.DungeonManager.KEY_DUNGEON_CHEST_MARKER;

public class InventoryListener implements Listener {
    public static final ResourceKey<LootTable> DUNGEON_LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse("lastlife:chests/dungeon_scaled"));

    @EventHandler(ignoreCancelled = true)
    public void onInventory(InventoryOpenEvent event) {
        Container container = ((CraftInventory)((CraftInventoryView<?, ?>)event.getView()).getTopInventory()).getInventory();

        if (container instanceof RandomizableContainerBlockEntity blockEntity) {
            Level level = blockEntity.getLevel();

            if (level == null || !level.dimension().location().toString().equals("lastlife:dungeon_dim")) {
                return;
            }
            CraftPersistentDataContainer pdc = blockEntity.persistentDataContainer;
            if (!pdc.getOrDefault(KEY_DUNGEON_CHEST_MARKER, PersistentDataType.BOOLEAN, false)) {
                return;
            }
            double current_chest_last_total = pdc.getOrDefault(KEY_DUNGEON_CHEST_LAST_DONATION_TOTAL, PersistentDataType.DOUBLE, 0.0);
            double dungeon_last_total = Utils.configs().DUNGEON_MANAGER.getLastDonationTotalFromMarker();

            // Dungeon total has increased since this chest was last opened
            if (dungeon_last_total > current_chest_last_total) {
                if (event.getPlayer() instanceof CraftHumanEntity craftPlayer) {
                    blockEntity.clearContent();
                    blockEntity.setLootTable(DUNGEON_LOOT_TABLE);
                    blockEntity.setLootTableSeed(level.random.nextLong());
                    blockEntity.unpackLootTable(craftPlayer.getHandle(), true);
                    pdc.set(KEY_DUNGEON_CHEST_LAST_DONATION_TOTAL, PersistentDataType.DOUBLE, dungeon_last_total);
                }
            }
        }
    }
}
