package com.dragon.lastlife.party;

import com.dragon.lastlife.config.PartyConfig;
import com.dragon.lastlife.players.Participant;
import com.dragon.lastlife.utils.Utils;
import com.quiptmc.core.config.ConfigMap;
import com.quiptmc.core.config.ConfigObject;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class Party extends ConfigObject {


    public ConfigMap<Participant> members = new ConfigMap<>();

    public Party() {

    }

    public Party(JSONObject json) {
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
        if(location == null)
            return;
        World world = location.getWorld();
        Optional<StructureTemplate> templateOptional = ((CraftWorld)world).getHandle().getStructureManager().get(ResourceLocation.parse("lastlife:features/mailbox"));
        if(templateOptional.isEmpty())
            return;
        StructureTemplate template = templateOptional.get();
        Mirror mirror = Mirror.values()[ThreadLocalRandom.current().nextInt(Mirror.values().length)];
        Rotation rot = Rotation.values()[ThreadLocalRandom.current().nextInt(Rotation.values().length)];

        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setMirror(mirror)
                .setRotation(rot)
                .setIgnoreEntities(false);

        BlockPos pos = new BlockPos(location.getBlockX()+2, location.getBlockY(), location.getBlockZ()+2);

        CraftWorld cw = (CraftWorld) world;
        boolean placed = template.placeInWorld(
                cw.getHandle(),
                pos,
                pos,
                settings,
                RandomSource.create(),
                2
        );
        System.out.println(placed);

    }
}
