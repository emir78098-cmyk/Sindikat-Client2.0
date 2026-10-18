package com.sindikat.module;

import com.sindikat.module.visual.Esp;
import com.sindikat.module.visual.Zoom;
import java.util.ArrayList;
import java.util.List;

public final class ModuleManager {
    public static final List<Module> ALL = new ArrayList<>();
    public static Esp ESP;
    public static Zoom ZOOM;

    private ModuleManager() {}

    public static void init() {
        ESP = reg(new Esp());
        ZOOM = reg(new Zoom());
    }

    private static <T extends Module> T reg(T m) { ALL.add(m); return m; }

    public static void tick() { for (Module m : ALL) m.onTick(); }

    public static List<Module> of(Category c) {
        List<Module> out = new ArrayList<>();
        for (Module m : ALL) if (m.category == c) out.add(m);
        return out;
    }
}
