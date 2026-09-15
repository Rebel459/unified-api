package net.rebel459.unified.impl.client.util;

import net.minecraft.core.Holder;
import net.rebel459.unified.impl.data.helper.MobVariants;

import java.util.Optional;

public interface LivingEntityRenderStateVariant {
    void setVariant(Optional<Holder<MobVariants.Definition>> variant);
    Optional<Holder<MobVariants.Definition>> getVariant();
}
