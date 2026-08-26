package net.rebel459.unified.mixin.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.rebel459.unified.util.data.impl.EntityRegistryImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MappedRegistry.class)
public class MappedRegistryMixin<T> {
    @Inject(method = "register", at = @At("RETURN"))
    @SuppressWarnings("unchecked")
    private void resolveCopiedEntities(ResourceKey<T> key, T value, RegistrationInfo registrationInfo, CallbackInfoReturnable<Holder.Reference<T>> cir) {
        if (value instanceof EntityType<?> entityType) {
            EntityRegistryImpl.onRegistered((ResourceKey<EntityType<?>>) (ResourceKey<?>) key, entityType);
        }
    }

    @Inject(method = "freeze", at = @At("HEAD"))
    private void validateCopiedEntities(CallbackInfoReturnable<?> cir) {
        MappedRegistry<?> registry = (MappedRegistry<?>) (Object) this;
        if (registry.key().equals(net.minecraft.core.registries.Registries.ENTITY_TYPE)) {
            EntityRegistryImpl.validateResolved();
        }
    }
}
