package com.dragon.lastlife.listeners;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.nms.CustomFox;
import com.dragon.lastlife.nms.NmsEntityFactory;
import com.dragon.lastlife.utils.Utils;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftFox;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.InvocationTargetException;

import static com.dragon.lastlife.nms.CustomFox.KEY_CUSTOM_FOX_MARKER;

/**
 * Restores our custom fox behavior across server restarts by replacing
 * vanilla foxes (reloaded from disk) that were previously marked as custom.
 */
public class FoxPersistenceListener implements Listener {
    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            if (entity instanceof Fox fox) {
                handleFoxEntity(fox);
            }
        }
    }

    public void handleFoxEntity(Fox fox) {
        // If it's already our CustomFox NMS instance, nothing to do
        if (((CraftEntity) fox).getHandle() instanceof CustomFox) return;

        try {
            CustomFox customFox = this.restoreCustomFoxFrom(fox);

            if (customFox != null) {
                // Remove the old vanilla fox
                fox.remove();
            }
        } catch (Exception ex) {
            Utils.initializer().getComponentLogger().error("Failed to restore CustomFox: ", ex);
        }
    }

    public CustomFox restoreCustomFoxFrom(org.bukkit.entity.Fox fox) {
        PersistentDataContainer pdc = fox.getPersistentDataContainer();

        Byte marker = pdc.get(KEY_CUSTOM_FOX_MARKER, PersistentDataType.BYTE);
        if (marker == null || marker == 0) return null;

        net.minecraft.world.entity.animal.Fox other = ((CraftFox) fox).getHandle();

        // Dump vanilla fox data into NBT
        TagValueOutput nbt = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        other.saveWithoutId(nbt);

        Location loc = fox.getLocation();
        CustomFox newFox;
        try {
            newFox = NmsEntityFactory.spawn(loc, CustomFox.class);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        // Load vanilla fox NBT into our custom Fox (includes all vanilla attributes + Bukkit Persistent storage)
        newFox.load(TagValueInput.create(ProblemReporter.DISCARDING, newFox.registryAccess(), nbt.buildResult()));

        return newFox;
    }
}
