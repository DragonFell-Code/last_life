package com.dragon.lastlife.config.object;

import com.quiptmc.core.config.ConfigObject;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class ConfigLocation extends ConfigObject {


    public double x, y, z;
    public float yaw, pitch;
    public String world;

    public ConfigLocation(String id) {
        super.id = id;
    }

    public ConfigLocation(String id, int blockX, int blockY, int blockZ, float yaw, float pitch, @NotNull String world) {
        this(id);
        this.x = blockX;
        this.y = blockY;
        this.z = blockZ;
        this.yaw = yaw;
        this.pitch = pitch;
        this.world = world;
    }

    public ConfigLocation(String id, double x, double y, double z, float yaw, float pitch, @NotNull String world) {
        this(id);
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.world = world;
    }

    public ConfigLocation(String id, @NotNull Location location) {
        this(id, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), location.getWorld().getName());
    }
}
