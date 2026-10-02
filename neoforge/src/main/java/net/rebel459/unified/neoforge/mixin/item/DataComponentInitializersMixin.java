package net.rebel459.unified.neoforge.mixin.item;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.rebel459.unified.impl.core.CommonEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiConsumer;

@Mixin(DataComponentInitializers.class)
public class DataComponentInitializersMixin {
    @Inject(method = "createInitializerForRegistry", at = @At("RETURN"), cancellable = true)
    private static <T> void addComponentModifiers(HolderLookup.Provider context, DataComponentInitializers.PendingComponentBuilders<T> elementBuilders, CallbackInfoReturnable<DataComponentInitializers.PendingComponents<T>> cir) {
        DataComponentInitializers.PendingComponents<T> pending = cir.getReturnValue();
        if (!pending.key().equals(Registries.ITEM)) return;

        cir.setReturnValue(new DataComponentInitializers.PendingComponents<>() {

            @Override
            public ResourceKey<? extends Registry<? extends T>> key() {
                return pending.key();
            }

            @Override
            public void forEach(BiConsumer<Holder.Reference<T>, DataComponentMap> output) {
                pending.forEach(output);
            }

            @Override
            public void apply() {
                pending.apply();
                context.lookupOrThrow(Registries.ITEM).listElements().forEach(holder -> {
                    DataComponentMap.Builder builder = DataComponentMap.builder().addAll(holder.components());
                    CommonEvents.DefaultDataComponents.passModify(holder.value(), builder, context);
                    holder.bindComponents(builder.build());
                });
            }
        });
    }
}
