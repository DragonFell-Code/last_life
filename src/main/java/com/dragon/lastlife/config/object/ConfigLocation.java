package com.dragon.lastlife.config.object;

import com.quiptmc.core.config.ConfigObject;
import org.jetbrains.annotations.NotNull;

public class ConfigLocation extends ConfigObject {


    public double x, y, z;
    public float yaw, pitch;
    public String world;

    public ConfigLocation() {

    }

    public ConfigLocation(int blockX, int blockY, int blockZ, float yaw, float pitch, @NotNull String name) {
        this.x = blockX;
        this.y = blockY;
        this.z = blockZ;
        this.yaw = yaw;
        this.pitch = pitch;
        this.world = name;
    }

    public ConfigLocation(double x, double y, double z, float yaw, float pitch, @NotNull String name) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.world = name;
    }
}
