package com.dragon.lastlife.config.factories;

import com.dragon.lastlife.utils.Utils;
import com.quiptmc.core.config.ConfigManager;
import com.quiptmc.core.config.ConfigObject;

public class Factories {

    public final ParticipantFactory PARTICIPANT_FACTORY = register(new ParticipantFactory());
    public final DonationFactory DONATION_FACTORY = register(new DonationFactory());
    public final WebhookFactory WEBHOOK_FACTORY = register(new WebhookFactory());

    public <T extends ConfigObject.Factory<?>> T register(T factory) {
        Utils.initializer().integration().log("Factories", "Registering factory: " + factory.getClass().getName());
        ConfigManager.registerFactory(factory);
        return factory;
    }

}
