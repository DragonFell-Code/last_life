package com.dragon.lastlife.utils.chat;

import com.quiptmc.core.data.JsonSerializable;
import net.kyori.adventure.text.format.TextColor;

import java.awt.*;

public class QuiptTextColor implements TextColor, JsonSerializable {

        public static final QuiptTextColor BLACK = new QuiptTextColor(0x000000);
        public static final QuiptTextColor DARK_BLUE = new QuiptTextColor(0x0000aa);
        public static final QuiptTextColor DARK_GREEN = new QuiptTextColor(0x00aa00);
        public static final QuiptTextColor DARK_AQUA = new QuiptTextColor(0x00aaaa);
        public static final QuiptTextColor DARK_RED = new QuiptTextColor(0xaa0000);
        public static final QuiptTextColor DARK_PURPLE = new QuiptTextColor(0xaa00aa);
        public static final QuiptTextColor GOLD = new QuiptTextColor(0xffaa00);
        public static final QuiptTextColor GRAY = new QuiptTextColor(0xaaaaaa);
        public static final QuiptTextColor DARK_GRAY = new QuiptTextColor(0x555555);
        public static final QuiptTextColor BLUE = new QuiptTextColor(0x5555ff);
        public static final QuiptTextColor GREEN = new QuiptTextColor(0x55ff55);
        public static final QuiptTextColor AQUA = new QuiptTextColor(0x55ffff);
        public static final QuiptTextColor RED = new QuiptTextColor(0xff5555);
        public static final QuiptTextColor LIGHT_PURPLE = new QuiptTextColor(0xff55ff);
        public static final QuiptTextColor YELLOW = new QuiptTextColor(0xffff55);
        public static final QuiptTextColor WHITE = new QuiptTextColor(0xffffff);
        public static final QuiptTextColor RESET = new QuiptTextColor(0xffffff);


        public int value;

        public QuiptTextColor(int value) {
                this.value = value;
        }

        public QuiptTextColor(){
                this.value = 0xffffff; // Default to white
        }

        @Override
        public int value() {
                return value;
        }
}
