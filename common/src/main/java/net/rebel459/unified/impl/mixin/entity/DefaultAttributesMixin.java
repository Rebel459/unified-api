package net.rebel459.unified.mixin.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.rebel459.unified.util.registry.EntityTypeCopies;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DefaultAttributes.class)
public class DefaultAttributesMixin {
    @Inject(method = "getSupplier", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings("unchecked")
    private static void copiedSupplier(EntityType<? extends LivingEntity> type, CallbackInfoReturnable<AttributeSupplier> cir) {
        EntityTypeCopies.template(type).ifPresent(template ->
                cir.setReturnValue(DefaultAttributes.getSupplier((EntityType<? extends LivingEntity>) template))
        );
    }

    @Inject(method = "hasSupplier", at = @At("HEAD"), cancellable = true)
    private static void copiedHasSupplier(EntityType<?> type, CallbackInfoReturnable<Boolean> cir) {
        EntityTypeCopies.template(type).ifPresent(template -> cir.setReturnValue(DefaultAttributes.hasSupplier(template)));
    }
}
