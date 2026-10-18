package com.sindikat.module;

public enum Category {
    COMBAT("Combat"), VISUALS("Visuals"), MOVEMENT("Movement"),
    PLAYER("Player"), WORLD("World"), MISC("Misc");
    public final String display;
    Category(String display) { this.display = display; }
}
