package net.rebel459.unified.mixin.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.rebel459.unified.util.helper.BlockConversions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(at = @At("HEAD"), method = "useOn", cancellable = true)
    private void useOnBlockConversions(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult result = BlockConversions.Impl.useOn(context);
        if (result != InteractionResult.PASS) cir.setReturnValue(result);
    }
}