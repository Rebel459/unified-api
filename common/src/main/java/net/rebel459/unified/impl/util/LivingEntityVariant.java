package net.rebel459.unified.impl.util;

import net.minecraft.core.Holder;
import net.minecraft.world.level.ServerLevelAccessor;
import net.rebel459.unified.impl.data.helper.MobVariants;

import java.util.Optional;

public interface LivingEntityVariant {
    void setVariant(Optional<Holder<MobVariants.Definition>> variant);
    Optional<Holder<MobVariants.Definition>> getVariant();
    void spawnVariant(ServerLevelAccessor level);
}
