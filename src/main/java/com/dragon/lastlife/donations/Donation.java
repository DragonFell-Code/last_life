package com.dragon.lastlife.donations;

import com.quiptmc.core.config.ConfigObject;
import com.quiptmc.core.data.JsonSerializable;
import org.json.JSONObject;

public class Donation extends ConfigObject {

    String displayName = "";
    String donorId = "";
    Links links = new Links();
    boolean isRegFee = false;
    int eventID = 0;
    String createdDateUTC = "";
    String recipientName = "";
    String recipientImageURL = "";
    int participantID = 0;
//    String amount = "0.00";
    String avatarImageURL = "";
    int teamID = 0;
    String donationID = "";
    String incentiveID = "";
    String message = "";

    public Donation(JSONObject json) {
        this.fromJson(json);
    }

    public static class Links implements JsonSerializable {
        String recipient = "";
        String donate = "";

        public Links(){

        }
        public Links(JSONObject json) {
            this.fromJson(json);
        }
    }
}
