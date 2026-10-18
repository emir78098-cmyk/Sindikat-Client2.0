package com.sindikat.module;

import net.minecraft.client.Minecraft;
import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public final String name;
    public final Category category;
    public final List<Setting> settings = new ArrayList<>();
    protected final Minecraft mc = Minecraft.getInstance();
    private boolean enabled;

    protected Module(String name, Category category) { this.name = name; this.category = category; }

    protected <T extends Setting> T add(T s) { settings.add(s); return s; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean on) {
        if (on == enabled) return;
        enabled = on;
        if (on) onEnable(); else onDisable();
    }
    public void toggle() { setEnabled(!enabled); }

    public void onTick() {}
    protected void onEnable() {}
    protected void onDisable() {}
}
