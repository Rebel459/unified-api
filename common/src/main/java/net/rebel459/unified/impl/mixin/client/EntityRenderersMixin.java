package net.rebel459.unified.mixin.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;
import net.rebel459.unified.util.registry.EntityTypeCopies;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(EntityRenderers.class)
public class EntityRenderersMixin {
    @Shadow @Final private static Map<EntityType<?>, EntityRendererProvider<?>> PROVIDERS;

    @Inject(method = "validateRegistrations", at = @At("HEAD"))
    private static void copyProvidersForValidation(CallbackInfoReturnable<Boolean> cir) {
        copyProviders();
    }

    @Inject(method = "createEntityRenderers", at = @At("HEAD"))
    private static void copyProvidersForCreation(CallbackInfoReturnable<?> cir) {
        copyProviders();
    }

    private static void copyProviders() {
        EntityTypeCopies.templates().forEach((copy, template) -> {
            EntityRendererProvider<?> provider = PROVIDERS.get(template);
            if (provider != null) PROVIDERS.putIfAbsent(copy, provider);
        });
    }
}
