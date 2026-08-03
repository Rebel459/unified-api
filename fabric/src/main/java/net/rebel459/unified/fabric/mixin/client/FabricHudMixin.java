package net.rebel459.unified.fabric.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.rebel459.unified.impl.client.core.ClientEventsImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class FabricHudMixin {

    @Inject(method = "extractHotbarAndDecorations", at = @At(value = "HEAD"))
    private void passRenderHotbar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        ClientEventsImpl.Hud.passRenderHotbar(Gui.class.cast(this), graphics, deltaTracker);
    }
}