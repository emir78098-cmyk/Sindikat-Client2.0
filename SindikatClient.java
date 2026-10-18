package com.sindikat;

import com.mojang.blaze3d.platform.InputConstants;
import com.sindikat.gui.ClickGuiScreen;
import com.sindikat.module.Config;
import com.sindikat.module.ModuleManager;
import com.sindikat.render.EspRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class SindikatClient implements ClientModInitializer {
    public static KeyMapping guiKey, zoomKey;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();
        Config.load();

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("sindikat", "main"));
        guiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.sindikat.gui", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        zoomKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.sindikat.zoom", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ModuleManager.tick();
            while (guiKey.consumeClick()) {
                if (client.screen == null) client.setScreen(new ClickGuiScreen());
            }
        });

        LevelRenderEvents.END_EXTRACTION.register(ctx -> ModuleManager.ESP.extract(ctx));
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(EspRenderer.get()::render);
    }
}
