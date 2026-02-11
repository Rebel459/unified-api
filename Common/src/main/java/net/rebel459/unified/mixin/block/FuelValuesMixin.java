package net.rebel459.unified.mixin.block;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FuelValues;
import net.rebel459.unified.registry.UnifiedComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FuelValues.class)
public class FuelValuesMixin {

    @Inject(at = @At("HEAD"), method = "isFuel", cancellable = true)
    private void burnComponent(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (itemStack.has(UnifiedComponents.FURNACE_FUEL.get())) {
            cir.setReturnValue(true);
        }
    }
}