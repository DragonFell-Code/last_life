package com.dragon.lastlife.loot;

import com.dragon.lastlife.party.Party;
import com.dragon.lastlife.utils.Utils;
import com.quiptmc.core.utils.TaskScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.bukkit.loot.LootTable;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class LootDelivery {

    public static final long MAX_DURATION = TimeUnit.MILLISECONDS.convert(2, TimeUnit.MINUTES);
    public static final int PARTICLE_DELAY = 125;

    private final Location location;
    private final LootTable table;
    private long started;


    private int ticks = 0;
    private boolean done = false;
    private double height = 20;
    private BlockDisplay display; // Spinning chest display

    public LootDelivery(Party party, LootTable table) {
        this.location = party.mailbox().clone().add(0.5,0,0.5);
        this.table = table;
    }

    public void start() {
        started = System.currentTimeMillis();
        Bukkit.getScheduler().runTaskLater(Utils.initializer(), () -> {
            if (!done) Bukkit.getScheduler().runTaskLater(Utils.initializer(), this::loop, 0);
        }, 0);
    }

    private void loop() {
        tick();
        if (!done) Bukkit.getScheduler().runTaskLater(Utils.initializer(), this::loop, 2);
    }

    private void tick() {
        // Lazily spawn the display on first tick
        if (display == null) {
            World world = location.getWorld();
            if (world != null) {
                // Spawn a BlockDisplay representing a chest at initial height
                Location spawnLoc = new Location(world, location.getX(), location.getY() + height, location.getZ());
                BlockDisplay bd = (BlockDisplay) world.spawnEntity(spawnLoc, EntityType.BLOCK_DISPLAY);
                BlockData chestData = Bukkit.createBlockData(Material.CHEST);
                bd.setBlock(chestData);
                // Center the pivot so rotation happens around the block's center (block size is 1x1x1, origin at min corner)
                // Translation of (-0.5, -0.5, -0.5) moves the model so its center aligns with the entity position
                bd.setTransformation(new Transformation(new Vector3f(-0.5f, -0.5f, -0.5f), new Quaternionf(), new Vector3f(1f, 1f, 1f), new Quaternionf()));
                display = bd;
            }
        }

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < 10; j++)
                location.getWorld().spawnParticle(Particle.END_ROD, location.x(), location.y() + Math.min(i * new Random().nextDouble(), height), location.z(), 1, 0, 0, 0, 0);
        }
        location.getWorld().spawnParticle(Particle.WITCH, location.x(), location.y() + height, location.z(), 10);


        //Remove magic numbers:
        double angle = ticks/3d;
        double radius = 1.5;

        location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                location.x() + Math.cos(angle) *radius,
                location.y() + height,
                location.z()+Math.sin(angle) *radius, 1, 0, 0, 0, 0);
        location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                location.x() + Math.cos(179+angle) *radius,
                location.y() + height,
                location.z()+Math.sin(179+angle) *radius, 1, 0, 0, 0, 0);
        // Update rotation and position of the display chest
        if (display != null && !display.isDead()) {
            // Spin around Y axis
            float radians = (float) (ticks / 6.0f); // slower spin
            Quaternionf yRot = new Quaternionf().rotateY(radians);
            // Keep pivot centered so the chest rotates around its center
            Transformation t = new Transformation(new Vector3f(-0.5f, -0.5f, -0.5f), yRot, new Vector3f(1f, 1f, 1f), new Quaternionf());
            display.setTransformation(t);
            // Move to current height above mailbox
            display.teleport(new Location(display.getWorld(), location.getX(), location.getY() + height, location.getZ()));
        }

        ticks = ticks + 1;
        height = height - 0.25;
        if(height < 0) stop();
        if (System.currentTimeMillis() - started >= MAX_DURATION) stop();
    }

    public boolean done() {
        return done;
    }

    public void stop() {
        done = true;
        if (display != null) {
            try {
                display.remove();
            } catch (Exception ignored) { }
            display = null;
        }
    }


}
