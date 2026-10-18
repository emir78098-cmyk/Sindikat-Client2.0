package com.sindikat.module;

public class ModeSetting extends Setting {
    public final String[] modes;
    private int index;
    public ModeSetting(String name, String... modes) { super(name); this.modes = modes; }
    public String get() { return modes[index]; }
    public boolean is(String m) { return modes[index].equals(m); }
    public void cycle() { index = (index + 1) % modes.length; }
    @Override public Object save() { return modes[index]; }
    @Override public void load(Object o) {
        if (o instanceof String s) for (int i = 0; i < modes.length; i++) if (modes[i].equals(s)) index = i;
    }
}
