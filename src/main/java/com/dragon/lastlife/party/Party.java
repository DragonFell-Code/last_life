package com.dragon.lastlife.party;

import com.dragon.lastlife.config.PartyConfig;
import com.dragon.lastlife.config.object.ConfigLocation;
import com.dragon.lastlife.loot.delivery.BundleDelivery;
import com.dragon.lastlife.loot.delivery.FoxDelivery;
import com.dragon.lastlife.nms.CustomFox;
import com.dragon.lastlife.nms.NmsEntityFactory;
import com.dragon.lastlife.players.Participant;
import com.dragon.lastlife.utils.Utils;
import com.quiptmc.core.config.ConfigMap;
import com.quiptmc.core.config.ConfigObject;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
// Use Bukkit meta API to set loot table on a shulker box and then convert to NMS
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.block.ShulkerBox;
import org.bukkit.loot.Lootable;

public class Party extends ConfigObject {


    public ConfigMap<Participant> members = new ConfigMap<>();
    public ConfigLocation mailbox;


    public Party() {
    }

    public Party(JSONObject json) {
        this();
        this.fromJson(json);
    }

    public void join(Participant participant) {
        PartyConfig config = Utils.configs().PARTY_CONFIG();
        Optional<Party> previousParty = config.get(participant);
        previousParty.ifPresent(party -> party.leave(participant));

        members.put(participant);
        Utils.configs().PARTY_CONFIG().save();
        Player player = participant.player().getPlayer();
        if(player != null)
            player.sendMessage(Utils.configs().MESSAGE_CONFIG.get("lastlife.party.join", this.id));
    }

    public void leave(Participant participant) {
        members.remove(participant);
        Utils.configs().PARTY_CONFIG().save();
        Player player = participant.player().getPlayer();
        if(player != null)
            player.sendMessage(Utils.configs().MESSAGE_CONFIG.get("lastlife.party.leave", this.id));

    }

    public void mailbox(@NotNull Location location) {
        World world = location.getWorld();
        Optional<StructureTemplate> templateOptional = ((CraftWorld)world).getHandle().getStructureManager().get(ResourceLocation.parse("lastlife:features/mailbox"));
        if(templateOptional.isEmpty())
            return;
        StructureTemplate template = templateOptional.get();
        Mirror mirror = Mirror.NONE;
        Rotation rot = Rotation.NONE;

        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setMirror(mirror)
                .setRotation(rot)
                .setIgnoreEntities(false);

        BlockPos pos = new BlockPos(location.getBlockX()-2, location.getBlockY(), location.getBlockZ()-2);

        CraftWorld cw = (CraftWorld) world;
        boolean placed = template.placeInWorld(
                cw.getHandle(),
                pos,
                pos,
                settings,
                RandomSource.create(),
                2
        );
        mailbox = new ConfigLocation(id + "-mailbox", location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getYaw(), location.getPitch(), location.getWorld().getName());
        Utils.configs().PARTY_CONFIG().save();
    }

    public Location mailbox(){
        return new Location(Bukkit.getWorld(mailbox.world), mailbox.x, mailbox.y, mailbox.z, mailbox.yaw, mailbox.pitch);
    }

    public void deliver(LootTable table) {
        if(new Random().nextDouble() < 0.25) new FoxDelivery(this, table).start();
        else new BundleDelivery(this, table).start();
    }
}
