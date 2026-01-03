package com.dragon.lastlife.loot.delivery;

import com.dragon.lastlife.loot.LootManager;
import com.dragon.lastlife.nms.CustomFox;
import com.dragon.lastlife.nms.NmsEntityFactory;
import com.dragon.lastlife.party.Party;
import com.dragon.lastlife.players.Participant;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.inventory.CraftItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class FoxDelivery extends DeliverySystem{

    private final Location mailbox;
    private final Party party;
    private final Participant participant;

    public FoxDelivery(Party party, Participant participant) {
        this.party = party;
        mailbox = party.mailbox().clone().add(0,1,0);
        this.participant = participant;
    }

    @Override
    void tick() {
        int radius = 30;
        // TODO: Randomize it ?
        Location spawn = mailbox.clone().add(
                ThreadLocalRandom.current().nextInt(radius) * (ThreadLocalRandom.current().nextBoolean() ? 1 : -1),
                ThreadLocalRandom.current().nextInt(radius)* (ThreadLocalRandom.current().nextBoolean() ? 1 : -1),
                ThreadLocalRandom.current().nextInt(radius)* (ThreadLocalRandom.current().nextBoolean() ? 1 : -1));
        while(!spawn.getBlock().getType().isSolid()){
            spawn = mailbox.clone().add(
                    new Random().nextInt(radius) * (new Random().nextBoolean() ? 1 : -1),
                    new Random().nextInt(radius)* (new Random().nextBoolean() ? 1 : -1),
                    new Random().nextInt(radius)* (new Random().nextBoolean() ? 1 : -1));
        }
        while (spawn.getBlock().getType().isSolid()) {
            // TODO: Also check for valid spawn ? (eg: not in a wall)
            // Does MC already has methods to locate a safe-spawn position ?
            spawn.add(0, 1, 0);
        }

        // Set a target – here we use the player's current location,
        // but this can be any arbitrary Location you pass in
        Location target = mailbox.clone().add(0,1,0);

        try {
            // Spawn our custom NMS fox with overridden AI that walks to target
            CustomFox fox = NmsEntityFactory.spawn(spawn, CustomFox.class);
            fox.setParty(party);
            Vec3 vecTarget = new Vec3(target.x(), target.y(), target.z());
            fox.deliverTo(vecTarget);

            org.bukkit.inventory.ItemStack bundle = LootManager.generateBundle(participant);
            // Convert to NMS for the fox to hold
            ItemStack nmsItem = CraftItemStack.asNMSCopy(bundle);
            fox.setItemSlot(EquipmentSlot.MAINHAND, nmsItem);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        stop();
    }
}
