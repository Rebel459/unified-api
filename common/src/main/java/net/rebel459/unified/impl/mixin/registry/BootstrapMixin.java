package net.rebel459.unified.impl.mixin.registry;

import net.minecraft.server.Bootstrap;
import net.rebel459.unified.Unified;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bootstrap.class)
public class BootstrapMixin {
    @Inject(method = "bootStrap", at = @At("RETURN"))
    private static void initializeUnifiedCodecs(CallbackInfo ci) {
        Unified.initCodecs();
    }
}
