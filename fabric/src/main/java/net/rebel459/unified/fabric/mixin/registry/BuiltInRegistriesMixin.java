package net.rebel459.unified.fabric.mixin.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.rebel459.unified.Unified;
import net.rebel459.unified.fabric.util.FabricInitializerState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltInRegistries.class)
public class BuiltInRegistriesMixin {
    @Inject(method = "freeze", at = @At("HEAD"))
    private static void completeUnifiedRegistries(CallbackInfo ci) {
        Unified.completeRegistries();
        Unified.init();
        FabricInitializerState.initializeCommon();
    }
}
