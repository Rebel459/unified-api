package net.rebel459.unified.api.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.codec.ExtensibleEntityCodec;
import net.rebel459.unified.impl.data.registry.EntityCopier;

public final class UnifiedEntityCodecs {
    private UnifiedEntityCodecs() {}

    public static final ExtensibleCodec.Complex<ExtensibleEntityCodec.Factory, ResourceKey<EntityType<?>>> COPY = ExtensibleCodecs.ENTITY.register(
            Unified.id("copy"),
            ResourceKey.codec(Registries.ENTITY_TYPE).fieldOf("base_entity"),
            base -> key -> {
                EntityCopier.declare(key, base);
                return EntityType.Builder.createNothing(MobCategory.MISC).dontTrackDeltas();
            }
    );

    public static void init() {}
}
