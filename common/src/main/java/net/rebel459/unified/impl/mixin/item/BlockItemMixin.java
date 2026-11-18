package net.rebel459.unified.impl.mixin.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.rebel459.unified.impl.core.CommonEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Inject(at = @At("HEAD"), method = "place", cancellable = true)
    private void passBeforePlace(BlockPlaceContext placeContext, CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult result = CommonEvents.Blocks.passOnPlace(placeContext);
        if (result != InteractionResult.PASS) cir.setReturnValue(result);
    }

    @Inject(at = @At("RETURN"), method = "place")
    private void passAfterPlace(BlockPlaceContext placeContext, CallbackInfoReturnable<InteractionResult> cir) {
        CommonEvents.Blocks.passAfterPlace(placeContext, cir.getReturnValue());
    }

    @Inject(at = @At("HEAD"), method = "useOn", cancellable = true)
    private void passUseOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult result = CommonEvents.Blocks.passOnUseOn(context);
        if (result != InteractionResult.PASS) cir.setReturnValue(result);
    }
}
