package net.rebel459.unified.mixin.server;

import net.minecraft.server.MinecraftServer;
import net.rebel459.unified.platform.FabricBiomeModifications;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Inject(method = "loadLevel", at = @At("HEAD"))
    private void applyBiomeModifications(CallbackInfo ci) {
        FabricBiomeModifications.apply((MinecraftServer) (Object) this);
    }
}
