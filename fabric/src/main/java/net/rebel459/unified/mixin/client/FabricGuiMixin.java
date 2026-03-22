package net.rebel459.unified.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.rebel459.unified.platform.client.ClientEventsImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class FabricGuiMixin {

    @Inject(method = "extractHotbarAndDecorations", at = @At(value = "HEAD"))
    private void passRenderHotbar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        ClientEventsImpl.Guis.passRenderHotbar(Gui.class.cast(this), graphics, deltaTracker);
    }
}