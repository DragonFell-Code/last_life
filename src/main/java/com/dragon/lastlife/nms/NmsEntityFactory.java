package com.dragon.lastlife.nms;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;

import java.lang.reflect.InvocationTargetException;

/**
 * Utilities for spawning custom NMS entities and bridging to Bukkit.
 */
public final class NmsEntityFactory {

    private NmsEntityFactory() {}

    /**
     * Spawns a CustomFox at the given spawn location and assigns it a target to walk to.
     * Returns the Bukkit wrapper entity.
     */
    public static <T extends NmsEntity> T spawn(Location spawn, Class<T> entityClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        ServerLevel level = ((CraftWorld) spawn.getWorld()).getHandle();

        T entity = entityClass.getConstructor(net.minecraft.world.level.Level.class).newInstance(level);
        entity.setPos(spawn);
        level.addFreshEntity((Entity) entity);
        return entity;
    }
}
