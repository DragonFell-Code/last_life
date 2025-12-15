package com.dragon.lastlife.loot.delivery;

import com.dragon.lastlife.utils.Utils;
import org.bukkit.Bukkit;

import java.util.concurrent.TimeUnit;

public abstract class DeliverySystem {

    public static final long MAX_DURATION = TimeUnit.MILLISECONDS.convert(2, TimeUnit.MINUTES);
    public static final int PARTICLE_DELAY = 125;

    long started;
    int ticks = 0;
    boolean done = false;

    public DeliverySystem() {

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

    abstract void tick();

    public boolean done() {
        return done;
    }

    public void stop() {
        done = true;
    }


}
