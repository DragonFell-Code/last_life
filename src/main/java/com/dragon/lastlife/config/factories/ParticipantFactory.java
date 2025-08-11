package com.dragon.lastlife.config.factories;

import com.dragon.lastlife.players.Participant;
import com.quiptmc.core.config.ConfigObject;
import org.json.JSONObject;

public class ParticipantFactory implements ConfigObject.Factory<Participant> {

    @Override
    public String getClassName() {
        return Participant.class.getName();
    }

    @Override
    public Participant createFromJson(JSONObject json) {
        Participant participant = new Participant();
        participant.fromJson(json);
        return participant;
    }
}
