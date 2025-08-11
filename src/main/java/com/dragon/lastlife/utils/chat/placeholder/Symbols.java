package com.dragon.lastlife.utils.chat.placeholder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum Symbols {
    SWORD("\uD83D\uDDE1", SymbolType.TOOL), BOW("\uD83C\uDFF9", SymbolType.TOOL), TRIDENT("\uD83D\uDD31", SymbolType.TOOL), POTION("\uD83E\uDDEA", SymbolType.TOOL), SPLASH_POTION("\u2697", SymbolType.TOOL), FISHING_ROD("\uD83C\uDFA3", SymbolType.TOOL), SHIELD("\uD83D\uDEE1", SymbolType.TOOL), AXE("\uD83E\uDE93", SymbolType.TOOL), STAR_1("\u2605", SymbolType.STAR), STAR_2("\u2606", SymbolType.STAR), STAR_3("\u272A", SymbolType.STAR), STAR_4("\u272A", SymbolType.STAR), STAR_5("\u272F", SymbolType.STAR), STAR_6("\u066D", SymbolType.STAR), STAR_7("\u272D", SymbolType.STAR), STAR_8("\u2730", SymbolType.STAR), STAR_9("\u269D", SymbolType.STAR), STAR_10("\u2734", SymbolType.STAR), STAR_11("\u2733", SymbolType.STAR), STAR_12("\u272B", SymbolType.STAR), STAR_13("\u235F", SymbolType.STAR), STAR_14("\u2727", SymbolType.STAR), STAR_15("\u2742", SymbolType.STAR), SUN("\u2600", SymbolType.STAR), NITRO("\u25C6", SymbolType.STAR), WARNING("\u26A0", SymbolType.TOOL), MULTIPLICATION("\u2716"), CHECK_MARK("\u2714"), MUSIC_NOTE_1("\u266A", SymbolType.MUSIC), MUSIC_NOTE_2("\u2669", SymbolType.MUSIC), MUSIC_NOTE_3("\u266B", SymbolType.MUSIC), MUSIC_NOTE_4("\u266C", SymbolType.MUSIC), SCISSORS_1("\u2704"), SCISSORS_2("\u2702"), ENVELOPE("\u2709"), COMET("\u2604"), SPARKLE_SMALL("\u0FCF"), SPARLE_LARGE("\u2042"), SPARKLE_CIRCLE("\uA670"), HEART_1("\u2764"), N0("\u24EA", SymbolType.NUMBER), N1("\u2460", SymbolType.NUMBER), N2("\u2461", SymbolType.NUMBER), N3("\u2462", SymbolType.NUMBER), N4("\u2463", SymbolType.NUMBER), N5("\u2464", SymbolType.NUMBER), N6("\u2465", SymbolType.NUMBER), N7("\u2466", SymbolType.NUMBER), N8("\u2467", SymbolType.NUMBER), N9("\u2468", SymbolType.NUMBER), N10("\u2469", SymbolType.NUMBER), N11("\u246A", SymbolType.NUMBER), N12("\u246B", SymbolType.NUMBER), N13("\u246C", SymbolType.NUMBER), N14("\u246D", SymbolType.NUMBER), N15("\u246E", SymbolType.NUMBER), N16("\u246F", SymbolType.NUMBER), N17("\u2470", SymbolType.NUMBER), N18("\u2471", SymbolType.NUMBER), N19("\u2472", SymbolType.NUMBER), N20("\u2473", SymbolType.NUMBER), BAR_0("\u2588", SymbolType.TEXT), BAR_1("\u258C", SymbolType.TEXT), BAR_2("\u258F", SymbolType.TEXT), GEMS("\u2743"), FLOWER1("\u273F"), FLOWER2("\u2740"), FLOWER3("\uD83E\uDD40"), SHAMROCK("\u2618"), UNKNOWN("???");

    final String unicode;
    final SymbolType[] types;

    Symbols(String unicode) {
        this(unicode, SymbolType.DEFAULT);
    }

    Symbols(String unicode, SymbolType... types) {
        this.unicode = unicode;
        this.types = types;
    }

    @Override
    public String toString() {
        return unicode;
    }

    public List<SymbolType> getTypes() {
        List<SymbolType> r = new ArrayList<>();
        Collections.addAll(r, types);
        return r;
    }

    public enum SymbolType {
        DEFAULT, TOOL, NUMBER, STAR, MUSIC, TEXT

    }
}
