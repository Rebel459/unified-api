package net.rebel459.unified.impl.mixin.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.rebel459.unified.impl.core.EventsImpl;
import net.rebel459.unified.api.event.EventTiming;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(at = @At("HEAD"), method = "use")
    private void passBeforeUse(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        EventsImpl.Items.passOnUse(EventTiming.PRE, level, player, hand);
    }

    @Inject(at = @At("TAIL"), method = "use")
    private void passAfterUse(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        EventsImpl.Items.passOnUse(EventTiming.POST, level, player, hand);
    }

    @Inject(at = @At("HEAD"), method = "useOn")
    private void passAfterUse(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        EventsImpl.Items.passOnUseOn(context);
    }
}