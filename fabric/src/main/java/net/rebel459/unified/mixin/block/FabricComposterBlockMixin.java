package net.rebel459.unified.mixin.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ComposterBlock;
import net.rebel459.unified.registry.UnifiedItemComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ComposterBlock.class)
public class FabricComposterBlockMixin {

    @WrapOperation(at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Object2FloatMap;containsKey(Ljava/lang/Object;)Z"), method = "useItemOn")
    private boolean compostComponentUseItemOn(Object2FloatMap instance, Object object, Operation<Boolean> original) {
        ItemStack stack = (ItemStack) object;
        return original.call(instance, stack) || (stack.has(UnifiedItemComponents.COMPOST.get()) && stack.get(UnifiedItemComponents.COMPOST.get()) > 0F);
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Object2FloatMap;containsKey(Ljava/lang/Object;)Z"), method = "insertItem")
    private static boolean compostComponentInsertItem(Object2FloatMap instance, Object object, Operation<Boolean> original) {
        ItemStack stack = (ItemStack) object;
        return original.call(instance, stack) || (stack.has(UnifiedItemComponents.COMPOST.get()) && stack.get(UnifiedItemComponents.COMPOST.get()) > 0F);
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Object2FloatMap;getFloat(Ljava/lang/Object;)F"), method = "addItem")
    private static float compostComponentAddItem(Object2FloatMap instance, Object object, Operation<Float> original) {
        ItemStack stack = (ItemStack) object;
        if (stack.has(UnifiedItemComponents.COMPOST.get())) {
            return stack.get(UnifiedItemComponents.COMPOST.get()).floatValue();
        }
        else {
            return original.call(instance, object);
        }
    }
}