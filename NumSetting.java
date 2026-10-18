package com.sindikat.module;

public class NumSetting extends Setting {
    public final double min, max, step;
    private double value;
    public NumSetting(String name, double value, double min, double max, double step) {
        super(name); this.min = min; this.max = max; this.step = step; set(value);
    }
    public double get() { return value; }
    public void set(double v) {
        v = Math.round(v / step) * step;
        value = Math.max(min, Math.min(max, v));
    }
    @Override public Object save() { return value; }
    @Override public void load(Object o) { if (o instanceof Number n) set(n.doubleValue()); }
}
