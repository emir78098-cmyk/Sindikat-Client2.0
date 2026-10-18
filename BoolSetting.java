package com.sindikat.module;

public class BoolSetting extends Setting {
    private boolean value;
    public BoolSetting(String name, boolean value) { super(name); this.value = value; }
    public boolean get() { return value; }
    public void toggle() { value = !value; }
    @Override public Object save() { return value; }
    @Override public void load(Object o) { if (o instanceof Boolean b) value = b; }
}
