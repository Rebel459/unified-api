package net.rebel459.unified.mixin.client;

import net.minecraft.client.gui.screens.TitleScreen;
import net.rebel459.unified.Unified;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class ExampleMixin {

    @Inject(at = @At("HEAD"), method = "init()V")
    private void init(CallbackInfo ci) {
        Unified.LOGGER.info("This line is printed by a mixin from Common!");
    }

}