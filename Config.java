package com.sindikat.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path path() { return FabricLoader.getInstance().getConfigDir().resolve("sindikat.json"); }

    private Config() {}

    public static void save() {
        try {
            Map<String, Object> root = new LinkedHashMap<>();
            for (Module m : ModuleManager.ALL) {
                Map<String, Object> o = new LinkedHashMap<>();
                o.put("enabled", m.isEnabled());
                for (Setting s : m.settings) o.put(s.name, s.save());
                root.put(m.name, o);
            }
            Files.writeString(path(), GSON.toJson(root));
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void load() {
        try {
            if (!Files.exists(path())) return;
            Map<String, Map<String, Object>> root = GSON.fromJson(Files.readString(path()),
                new TypeToken<Map<String, Map<String, Object>>>() {}.getType());
            if (root == null) return;
            for (Module m : ModuleManager.ALL) {
                Map<String, Object> o = root.get(m.name);
                if (o == null) continue;
                if (o.get("enabled") instanceof Boolean b) m.setEnabled(b);
                for (Setting s : m.settings) if (o.containsKey(s.name)) s.load(o.get(s.name));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
