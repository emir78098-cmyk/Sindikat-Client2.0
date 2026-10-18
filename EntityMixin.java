package com.sindikat.mixin;

import com.sindikat.module.ModuleManager;
import com.sindikat.module.visual.Esp;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
    private void sindikat$glow(CallbackInfoReturnable<Boolean> cir) {
        Esp esp = ModuleManager.ESP;
        if (esp != null && esp.shouldGlow((Entity) (Object) this)) cir.setReturnValue(true);
    }

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void sindikat$color(CallbackInfoReturnable<Integer> cir) {
        Esp esp = ModuleManager.ESP;
        Entity e = (Entity) (Object) this;
        if (esp != null && esp.shouldGlow(e)) cir.setReturnValue(esp.color(e));
    }
}
