package net.rebel459.unified.impl.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.rebel459.unified.impl.client.core.ClientEventsImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    @Inject(method = "init", at = @At(value = "TAIL"))
    private void passInitAbstractContainerScreen(CallbackInfo ci) {
        ClientEventsImpl.Screens.passInitAbstractContainerScreen(AbstractContainerScreen.class.cast(this));
    }
}