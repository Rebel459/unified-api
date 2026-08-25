package net.rebel459.unified.client.util.mixin;

import net.minecraft.core.Holder;
import net.rebel459.unified.util.data.MobVariants;

import java.util.Optional;

public interface LivingEntityRenderStateVariant {
    void setVariant(Optional<Holder<MobVariants.Variant>> variant);
    Optional<Holder<MobVariants.Variant>> getVariant();
}
