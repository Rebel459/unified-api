package net.rebel459.unified.impl.mixin.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.rebel459.unified.impl.core.EventsImpl;
import net.rebel459.unified.api.event.EventTiming;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Inject(at = @At("HEAD"), method = "place")
    private void passBeforePlace(BlockPlaceContext placeContext, CallbackInfoReturnable<InteractionResult> cir) {
        EventsImpl.Blocks.passOnPlace(EventTiming.PRE, placeContext);
    }

    @Inject(at = @At("TAIL"), method = "place")
    private void passAfterPlace(BlockPlaceContext placeContext, CallbackInfoReturnable<InteractionResult> cir) {
        EventsImpl.Blocks.passOnPlace(EventTiming.POST, placeContext);
    }

    @Inject(at = @At("HEAD"), method = "useOn")
    private void passAfterUse(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        EventsImpl.Blocks.passOnUseOn(context);
    }
}