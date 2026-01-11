package com.dragon.lastlife.loot.delivery;

import com.dragon.lastlife.loot.LootManager;
import com.dragon.lastlife.party.Party;
import com.dragon.lastlife.players.Participant;
import com.dragon.lastlife.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;

public class BundleDelivery extends DeliverySystem {

    private final Location location;
    private final Participant participant;
    private ArmorStand display; // Spinning chest display


    private double height = 20;

    public BundleDelivery(Party party, Participant participant) {
        this.location = party.mailbox().getBlock().getLocation().clone().add(0.5, 1, 0.5);
        this.participant = participant;
        location.getChunk().load(true);
            display = location.getWorld().spawn(location, ArmorStand.class);
            display.setGravity(false);
            display.setMarker(true);
            display.setInvisible(true);
            display.setInvulnerable(true);
    }

    @Override
    public void start() {
        ItemStack itemStack = new ItemStack(Material.CHEST);
        display.getEquipment().setHelmet(itemStack);
        super.start();
    }

    public void tick() {
        location.getChunk().load(false);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < 10; j++)
                location.getWorld().spawnParticle(Particle.END_ROD, location.x(), location.y() + Math.min(i * new Random().nextDouble(), height), location.z(), 1, 0, 0, 0, 0);
        }
        location.getWorld().spawnParticle(Particle.WITCH, location.x(), location.y() + height, location.z(), 10);

        //Remove magic numbers:
        double angle = ticks / 3d;
        double radius = 1.5;

        location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, location.x() + Math.cos(angle) * radius, location.y() + height, location.z() + Math.sin(angle) * radius, 1, 0, 0, 0, 0);
        location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, location.x() + Math.cos(179 + angle) * radius, location.y() + height, location.z() + Math.sin(179 + angle) * radius, 1, 0, 0, 0, 0);
        // Update rotation and position of the display chest
        if (display != null && !display.isDead()) {
            // Spin around Y axis
            float radians = ticks / 6.0f; // slower spin
            display.setHeadPose(new EulerAngle(0, radians, 0));
            // Move to current height above mailbox
            display.teleport(new Location(display.getWorld(), location.getX(), location.getY() + height - 2, location.getZ()));
        }

        ticks = ticks + 1;
        height = height - 0.25;
        if (height < 0 || System.currentTimeMillis() - started >= MAX_DURATION) stop();
    }

    @Override
    public void stop() {
        super.stop();
        if (location.getBlock().getType() != Material.CHEST) {
            if (location.getBlock().getType() != Material.AIR)
                location.getWorld().dropItem(location, new ItemStack(location.getBlock().getType())).setUnlimitedLifetime(true);
            location.getBlock().setType(Material.CHEST);
        }
        Chest chest = (Chest) location.getBlock().getState();
        ItemStack bundle = LootManager.generateBundle(participant);

        HashMap<Integer, ItemStack> failed = chest.getBlockInventory().addItem(bundle);

        if (!failed.isEmpty()) {
            location.getWorld().dropItem(location, bundle).setUnlimitedLifetime(true);
        }
        if (display != null) {
            try {
                display.remove();
            } catch (Exception e) {
                Utils.initializer().getLogger().log(Level.WARNING, "Failed to remove entity", e);
            }
            display = null;
        }
    }
}
