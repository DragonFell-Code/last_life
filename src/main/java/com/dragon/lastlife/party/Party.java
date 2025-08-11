package com.dragon.lastlife.party;

import com.dragon.lastlife.utils.chat.QuiptTextColor;
import com.quiptmc.core.data.JsonSerializable;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

public class Party implements JsonSerializable {


    public String id;

    public String name;
    public QuiptTextColor color;

    public Party(String admin, String admin1, QuiptTextColor color) {
        this.id = admin;
        this.name = admin1;
        this.color = color;
    }

    public Party(){

    }

    public Party(String id, String name, int color){
        this.id = id;
        this.name = name;
        this.color = new QuiptTextColor(color);
    }
}
