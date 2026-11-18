package net.rebel459.unified.impl.mixin.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.rebel459.unified.impl.core.CommonEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(at = @At("HEAD"), method = "useOn", cancellable = true)
    private void useOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult result = CommonEvents.Items.passOnUseOn(context);
        if (result != InteractionResult.PASS) cir.setReturnValue(result);
    }

    @Inject(at = @At("HEAD"), method = "use", cancellable = true)
    private void use(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult result = CommonEvents.Items.passOnUse(level, player, hand);
        if (result != InteractionResult.PASS) cir.setReturnValue(result);
    }

    @Inject(at = @At("RETURN"), method = "use")
    private void afterUse(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        CommonEvents.Items.passAfterUse(level, player, hand, cir.getReturnValue());
    }
}
