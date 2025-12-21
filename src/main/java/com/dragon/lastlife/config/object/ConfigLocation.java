package com.dragon.lastlife.config.object;

import com.quiptmc.core.annotations.NotNull;
import com.quiptmc.core.config.ConfigObject;
import com.quiptmc.core.data.JsonSerializable;
import org.bukkit.Location;
import org.json.JSONObject;

import java.math.BigDecimal;

public class ConfigLocation extends ConfigObject implements JsonSerializable {


    public double x;
    public double y;
    public double z;
    public BigDecimal yaw;
    public BigDecimal pitch;
    public String world;

    public ConfigLocation(String id, Location location) {
        super.id = id;
        this.x = location.x();
        this.y = location.y();
        this.z = location.z();
        this.yaw = BigDecimal.valueOf(location.getYaw());
        this.pitch = BigDecimal.valueOf(location.getPitch());
        this.world = location.getWorld().getName();
    }


    /**
     * For JSON deserialization only
     */
    public ConfigLocation(){

    }

    public ConfigLocation(JSONObject json) {
        fromJson(json);
    }

    public ConfigLocation(String id) {
        super.id = id;
    }

    public ConfigLocation(String id, int blockX, int blockY, int blockZ, float yaw, float pitch, @NotNull String world) {
        this(id);
        this.x = blockX;
        this.y = blockY;
        this.z = blockZ;
        this.yaw = BigDecimal.valueOf(yaw);
        this.pitch = BigDecimal.valueOf(pitch);
        this.world = world;
    }

    public ConfigLocation(String id, double x, double y, double z, float yaw, float pitch, @NotNull String world) {
        this(id);
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = BigDecimal.valueOf(yaw);
        this.pitch = BigDecimal.valueOf(pitch);
        this.world = world;
    }

}
