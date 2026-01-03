package com.dragon.lastlife.loot.delivery;

import com.dragon.lastlife.loot.LootManager;
import com.dragon.lastlife.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.time.Duration;

import static com.dragon.lastlife.utils.Utils.configs;

public class ShulkerDelivery extends DeliverySystem {

    Location location;

    public ShulkerDelivery(Location location) {
        this.location = location;
    }

    @Override
    void tick() {
        LootManager.generateShulker(location);

        // TODO: Add announcement
        Utils.initializer().integration().log("DonationFlutter", "Shulker delivery to [" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + "]");
        stop();
    }
}
