package net.rebel459.unified.mixin.block;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import net.rebel459.unified.registry.UnifiedComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {

    @Inject(at = @At("HEAD"), method = "getBurnDuration", cancellable = true)
    private void burnComponent(FuelValues fuelValues, ItemStack itemStack, CallbackInfoReturnable<Integer> cir) {
        if (itemStack.has(UnifiedComponents.FURNACE_FUEL.get())) {
            cir.setReturnValue(itemStack.get(UnifiedComponents.FURNACE_FUEL.get()));
        }
    }
}