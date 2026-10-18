package com.sindikat.mixin;

import com.sindikat.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Zoom: подменяем значение настройки FOV, пока зум активен. */
@Mixin(OptionInstance.class)
public class OptionInstanceMixin {
    @Inject(method = "get", at = @At("RETURN"), cancellable = true)
    private void sindikat$zoom(CallbackInfoReturnable<Object> cir) {
        if (ModuleManager.ZOOM == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.options == null || (Object) this != mc.options.fov()) return;
        cir.setReturnValue(ModuleManager.ZOOM.modifyFov(cir.getReturnValue()));
    }
}
