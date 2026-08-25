package net.rebel459.unified.impl.client.util;

import net.minecraft.core.Holder;
import net.rebel459.unified.impl.registry.MobVariants;

import java.util.Optional;

public interface LivingEntityRenderStateVariant {
    void setVariant(Optional<Holder<MobVariants.Variant>> variant);
    Optional<Holder<MobVariants.Variant>> getVariant();
}
