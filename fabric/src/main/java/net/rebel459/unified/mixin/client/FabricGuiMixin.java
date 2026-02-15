package net.rebel459.unified.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.rebel459.unified.platform.client.ClientEventsImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
    public abstract class FabricGuiMixin {

        @Inject(method = "renderHotbarAndDecorations", at = @At(value = "HEAD"))
        private void passHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
            ClientEventsImpl.Gui.passHotbar(Gui.class.cast(this), guiGraphics, deltaTracker);
        }
    }