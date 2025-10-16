package com.dragon.lastlife.listeners;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.nms.CustomFox;
import com.dragon.lastlife.nms.NmsEntityFactory;
import com.dragon.lastlife.utils.Utils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Restores our custom fox behavior across server restarts by replacing
 * vanilla foxes (reloaded from disk) that were previously marked as custom.
 */
public class FoxPersistenceListener implements Listener {

    private final NamespacedKey KEY_MARKER = new NamespacedKey(Utils.initializer(), "is_custom_fox");
    private final NamespacedKey KEY_STATE = new NamespacedKey(Utils.initializer(), "fox_state");
    private final NamespacedKey KEY_TX = new NamespacedKey(Utils.initializer(), "fox_target_x");
    private final NamespacedKey KEY_TY = new NamespacedKey(Utils.initializer(), "fox_target_y");
    private final NamespacedKey KEY_TZ = new NamespacedKey(Utils.initializer(), "fox_target_z");

    public FoxPersistenceListener(Initializer initializer) {
        initializer.getServer().getPluginManager().registerEvents(this, initializer);
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            if (!(entity instanceof Fox fox)) continue;

            // If it's already our CustomFox NMS instance, nothing to do
            if (((CraftEntity) fox).getHandle() instanceof CustomFox) continue;

            PersistentDataContainer pdc = fox.getPersistentDataContainer();
            Byte marker = pdc.get(KEY_MARKER, PersistentDataType.BYTE);
            if (marker == null || marker == 0) continue;

            // Restore target/state from PDC
            Double tx = pdc.get(KEY_TX, PersistentDataType.DOUBLE);
            Double ty = pdc.get(KEY_TY, PersistentDataType.DOUBLE);
            Double tz = pdc.get(KEY_TZ, PersistentDataType.DOUBLE);
            String state = pdc.get(KEY_STATE, PersistentDataType.STRING);

            Location loc = fox.getLocation();

            try {
                CustomFox nmsFox = NmsEntityFactory.spawn(loc, CustomFox.class);
                // Copy equipment main hand if any
                if (fox.getEquipment() != null && fox.getEquipment().getItemInMainHand() != null) {
                    ((Fox) nmsFox.getBukkitEntity()).getEquipment().setItemInMainHand(fox.getEquipment().getItemInMainHand());
                }

                // Apply target/state if present
                if (tx != null && ty != null && tz != null) {
                    nmsFox.setTarget(new net.minecraft.world.phys.Vec3(tx, ty, tz));
                }
                if (state != null) {
                    try {
                        nmsFox.state(CustomFox.State.valueOf(state));
                    } catch (IllegalArgumentException ignored) {}
                }

                // Reapply marker and data to new Bukkit fox entity
                Fox newBukkitFox = (Fox) nmsFox.getBukkitEntity();
                CustomFox.writePersistentData(newBukkitFox, nmsFox.state(), nmsFox.getDeliveryTarget());

                // Remove the old vanilla fox
                fox.remove();
            } catch (Exception ex) {
                Utils.initializer().getComponentLogger().error("Failed to restore CustomFox: " + ex.getMessage());
            }
        }
    }
}
