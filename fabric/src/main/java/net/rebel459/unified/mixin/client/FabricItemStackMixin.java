package net.rebel459.unified.mixin.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.rebel459.unified.client.util.FabricAttributeTooltipImpl;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class FabricItemStackMixin {

    @Inject(method = "addAttributeTooltips", at = @At("HEAD"))
    private void beginAttributeTooltip(Consumer<Component> consumer, TooltipDisplay display, @Nullable Player player, CallbackInfo ci) {
        FabricAttributeTooltipImpl.setStack(ItemStack.class.cast(this));
    }

    @Inject(method = "addAttributeTooltips", at = @At("TAIL"))
    private void endAttributeTooltip(Consumer<Component> consumer, TooltipDisplay display, @Nullable Player player, CallbackInfo ci) {
        FabricAttributeTooltipImpl.clearStack();
    }
}
