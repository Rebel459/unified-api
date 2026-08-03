package net.rebel459.unified.neoforge.mixin.block;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ComposterBlock;
import net.rebel459.unified.api.registry.UnifiedDataComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ComposterBlock.class)
public class NeoForgeComposterBlockMixin {

    @Inject(at = @At(value = "HEAD"), method = "getValue", cancellable = true)
    private static void compostComponentUseItemOn(ItemStack item, CallbackInfoReturnable<Float> cir) {
        if (item.has(UnifiedDataComponents.COMPOST.get())) {
            cir.setReturnValue(item.get(UnifiedDataComponents.COMPOST.get()));
        }
    }
}