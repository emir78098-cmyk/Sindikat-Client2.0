package com.sindikat.module.visual;

import com.sindikat.SindikatClient;
import com.sindikat.module.*;
import com.sindikat.module.Module;

public class Zoom extends Module {
    public final NumSetting factor = add(new NumSetting("Factor", 4, 1.5, 10, 0.5));
    public final BoolSetting smooth = add(new BoolSetting("Smooth", true));

    private double current = 1.0;
    private long last = System.nanoTime();

    public Zoom() { super("Zoom", Category.VISUALS); }

    public boolean active() {
        return isEnabled() && mc.screen == null
            && SindikatClient.zoomKey != null && SindikatClient.zoomKey.isDown();
    }

    /** Вызывается миксином при чтении настройки FOV. Принимает Integer/Double, возвращает изменённое значение. */
    public Object modifyFov(Object value) {
        long now = System.nanoTime();
        double dt = Math.min((now - last) / 1e9, 0.1);
        last = now;
        double target = active() ? factor.get() : 1.0;
        current = smooth.get() ? current + (target - current) * (1 - Math.exp(-dt * 14)) : target;
        if (current <= 1.001 || mc.screen != null) return value;
        if (value instanceof Integer i) return (int) Math.max(1, Math.round(i / current));
        if (value instanceof Double d) return Math.max(1.0, d / current);
        return value;
    }
}
