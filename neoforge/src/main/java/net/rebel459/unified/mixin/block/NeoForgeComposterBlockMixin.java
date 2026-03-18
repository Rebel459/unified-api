package net.rebel459.unified.mixin.block;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ComposterBlock;
import net.rebel459.unified.registry.UnifiedItemComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ComposterBlock.class)
public class NeoForgeComposterBlockMixin {

    @Inject(at = @At(value = "HEAD"), method = "getValue", cancellable = true)
    private static void compostComponentUseItemOn(ItemStack stack, CallbackInfoReturnable<Float> cir) {
        if (stack.has(UnifiedItemComponents.COMPOST.get())) {
            cir.setReturnValue(stack.get(UnifiedItemComponents.COMPOST.get()).floatValue());
        }
    }
}