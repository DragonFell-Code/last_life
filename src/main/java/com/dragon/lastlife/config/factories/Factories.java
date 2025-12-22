package com.dragon.lastlife.config.factories;

import com.dragon.lastlife.donations.Donation;
import com.dragon.lastlife.party.Party;
import com.dragon.lastlife.players.Participant;
import com.dragon.lastlife.utils.Utils;
import com.quiptmc.core.config.ConfigManager;
import com.quiptmc.core.config.ConfigObject;
import com.quiptmc.core.config.factories.ConfigStringFactory;
import com.quiptmc.core.config.factories.GenericFactory;
import com.quiptmc.core.discord.Webhook;

public class Factories {

    public Factories() {
        register(new GenericFactory<>(Participant.class));
        register(new GenericFactory<>(Donation.class));
        register(new GenericFactory<>(Webhook.class));
        register(new GenericFactory<>(Party.class));
        register(new ConfigStringFactory());
        register(new ConfigLocationFactory());
    }

    public <T extends ConfigObject.Factory<?>> void register(T factory) {
        Utils.initializer().integration().log("Factories", "Registering factory: " + factory.getClass().getName());
        ConfigManager.registerFactory(factory);
    }

}
