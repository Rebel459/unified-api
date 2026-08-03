package net.rebel459.unified.impl.mixin.client;

import net.minecraft.client.player.LocalPlayer;
import net.rebel459.unified.impl.client.core.ClientEventsImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Inject(method = "respawn", at = @At(value = "TAIL"))
    private void passOnRespawn(CallbackInfo ci) {
        LocalPlayer player = LocalPlayer.class.cast(this);
        ClientEventsImpl.Instance.passOnRespawn(player);
    }
}