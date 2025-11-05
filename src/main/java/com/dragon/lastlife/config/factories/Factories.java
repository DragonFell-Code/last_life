package com.dragon.lastlife.config.factories;

import com.dragon.lastlife.donations.Donation;
import com.dragon.lastlife.party.Party;
import com.dragon.lastlife.players.Participant;
import com.dragon.lastlife.utils.Utils;
import com.quiptmc.core.config.ConfigManager;
import com.quiptmc.core.config.ConfigObject;
import com.quiptmc.core.config.factories.GenericFactory;
import com.quiptmc.core.config.objects.ConfigString;
import com.quiptmc.core.discord.Webhook;

public class Factories {

    public final ConfigObject.Factory<Participant> PARTICIPANT_FACTORY = register(new GenericFactory<>(Participant.class));
    public final ConfigObject.Factory<Donation> DONATION_FACTORY = register(new GenericFactory<>(Donation.class));
    public final ConfigObject.Factory<Webhook> WEBHOOK_FACTORY = register(new GenericFactory<>(Webhook .class));
    public final ConfigObject.Factory<Party> PARTY_FACTORY = register(new GenericFactory<>(Party.class));
    public final ConfigObject.Factory<ConfigString> CONFIG_STRING_FACTORY = register(new ConfigStringFactory());

    public <T extends ConfigObject.Factory<?>> T register(T factory) {
        Utils.initializer().integration().log("Factories", "Registering factory: " + factory.getClass().getName());
        ConfigManager.registerFactory(factory);
        return factory;
    }

}
