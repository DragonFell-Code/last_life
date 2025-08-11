package com.dragon.lastlife.utils;

import com.dragon.lastlife.Initializer;
import com.dragon.lastlife.config.Configs;
import com.dragon.lastlife.utils.chat.MessageUtils;

public class Utils {

    private static Initializer initializer;
    private static Configs configs;
//    private static

    public static void init(Initializer init) {
        initializer = init;
        configs = new Configs(init);
        MessageUtils.start();
    }

    public static Initializer initializer(){
        return initializer;
    }

    public static Configs configs() {
        return configs;
    }
}
