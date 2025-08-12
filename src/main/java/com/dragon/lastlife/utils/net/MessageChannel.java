package com.dragon.lastlife.utils.net;

public class MessageChannel {

    public final Type type;
    public final String name;

    MessageChannel(Type type, String name){
        this.type = type;
        this.name = name;
    }

    public enum Type {
        INCOMING,
        OUTGOING
    }
}
