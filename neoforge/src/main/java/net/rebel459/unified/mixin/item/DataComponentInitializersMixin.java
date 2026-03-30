package net.rebel459.unified.mixin.item;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentInitializers;
import net.rebel459.unified.platform.NeoForgeUnifiedEvents;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DataComponentInitializers.class)
public abstract class DataComponentInitializersMixin {

    @WrapMethod(method = "createInitializerForRegistry")
    private static <T> DataComponentInitializers.PendingComponents<T> captureLookup(HolderLookup.Provider context, DataComponentInitializers.PendingComponentBuilders<T> elementBuilders, Operation<DataComponentInitializers.PendingComponents<T>> original) {
        return ScopedValue.where(NeoForgeUnifiedEvents.DEFAULT_ITEM_COMPONENTS_LOOKUP_PROVIDER, context).call(() -> original.call(context, elementBuilders));
    }
}
