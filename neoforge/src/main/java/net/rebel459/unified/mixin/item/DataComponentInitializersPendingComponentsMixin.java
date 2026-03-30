package net.rebel459.unified.mixin.item;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.rebel459.unified.platform.NeoForgeUnifiedEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(targets = "net.minecraft.core.component.DataComponentInitializers$1")
public abstract class DataComponentInitializersPendingComponentsMixin<T> {

    @Unique
    private HolderLookup.Provider registryLookup;

    @Shadow
    public abstract ResourceKey<? extends Registry<?>> key();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void store(ResourceKey<T> key, List<DataComponentInitializers.BakedEntry<T>> entries, CallbackInfo ci) {
        this.registryLookup = NeoForgeUnifiedEvents.DEFAULT_ITEM_COMPONENTS_LOOKUP_PROVIDER.get();
    }

    @Inject(method = "apply", at = @At("RETURN"))
    private void apply(CallbackInfo ci) {
        if (Registries.ITEM.equals(key())) {
            NeoForgeUnifiedEvents.modifyDefaultItemComponentsEvent(registryLookup);
        }
    }
}
