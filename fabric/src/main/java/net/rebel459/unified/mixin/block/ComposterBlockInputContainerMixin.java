package net.rebel459.unified.mixin.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.rebel459.unified.registry.UnifiedDataComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ComposterBlock.InputContainer.class)
public class ComposterBlockInputContainerMixin {

    @WrapOperation(at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Object2FloatMap;containsKey(Ljava/lang/Object;)Z"), method = "canPlaceItemThroughFace")
    private boolean compostComponentUseItemOn(Object2FloatMap instance, Object object, Operation<Boolean> original) {
        ItemStack stack = getStack(object);
        return original.call(instance, stack) || (stack.has(UnifiedDataComponents.COMPOST.get()) && stack.get(UnifiedDataComponents.COMPOST.get()) > 0F);
    }

    @Unique
    private static ItemStack getStack(Object object) {
        return switch (object) {
            case ItemStack itemStack -> itemStack;
            case Item item -> item.getDefaultInstance();
            case Block block -> block.asItem().getDefaultInstance();
            case null, default -> ItemStack.EMPTY;
        };
    }
}