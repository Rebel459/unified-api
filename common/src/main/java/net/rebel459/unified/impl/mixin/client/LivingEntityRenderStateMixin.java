package net.rebel459.unified.impl.mixin.client;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.Holder;
import net.rebel459.unified.impl.client.util.LivingEntityRenderStateVariant;
import net.rebel459.unified.impl.registry.MobVariants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements LivingEntityRenderStateVariant {

    @Unique
    private Optional<Holder<MobVariants.Variant>> unified$variant = Optional.empty();

    @Override
    public void setVariant(Optional<Holder<MobVariants.Variant>> variant) {
        this.unified$variant = variant;
    }

    @Override
    public Optional<Holder<MobVariants.Variant>> getVariant() {
        return this.unified$variant;
    }
}
