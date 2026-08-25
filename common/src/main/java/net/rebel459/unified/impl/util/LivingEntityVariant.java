package net.rebel459.unified.impl.util;

import net.minecraft.core.Holder;
import net.minecraft.world.level.ServerLevelAccessor;
import net.rebel459.unified.impl.registry.MobVariants;

import java.util.Optional;

public interface LivingEntityVariant {
    void setVariant(Optional<Holder<MobVariants.Variant>> variant);
    Optional<Holder<MobVariants.Variant>> getVariant();
    void spawnVariant(ServerLevelAccessor level);
}
