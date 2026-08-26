package net.rebel459.unified.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.rebel459.unified.client.util.mixin.LivingEntityRenderStateVariant;
import net.rebel459.unified.util.mixin.LivingEntityVariant;
import net.rebel459.unified.util.registry.EntityTypeCopies;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void copyVariantToRenderState(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
        var variant = ((LivingEntityVariant) entity).getVariant();
        ((LivingEntityRenderStateVariant) state).setVariant(variant);
    }

    @WrapOperation(method = "getRenderType", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getTextureLocation(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)Lnet/minecraft/resources/Identifier;"))
    private Identifier getVariantTexture(LivingEntityRenderer<?, ?, ?> renderer, LivingEntityRenderState state, Operation<Identifier> original) {
        Identifier originalTexture = original.call(renderer, state);
        return ((LivingEntityRenderStateVariant) state).getVariant()
                .filter(variant -> variant.value().target()
                        .map(EntityType.getKey(state.entityType)::equals)
                        .orElseGet(() -> EntityTypeCopies.isDefaultVariant(state.entityType, variant)))
                .flatMap(variant -> state.isBaby ? variant.value().babyTexture() : variant.value().texture())
                .filter(texture -> texture.original().map(originalId -> getTexture(originalId).equals(originalTexture)).orElse(true))
                .map(texture -> getTexture(texture.replacement()))
                .orElse(originalTexture);
    }

    @Unique
    private Identifier getTexture(Identifier id) {
        return Identifier.fromNamespaceAndPath(id.getNamespace(), "textures/" + id.getPath() + ".png");
    }
}
