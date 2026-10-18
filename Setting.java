package com.sindikat.module;

public abstract class Setting {
    public final String name;
    protected Setting(String name) { this.name = name; }
    public abstract Object save();
    public abstract void load(Object o);
}
