package net.rebel459.unified.util.data.registry;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.rebel459.unified.Unified;
import net.rebel459.unified.platform.InternalHandlerImpl;
import net.rebel459.unified.util.data.MobVariants;
import net.rebel459.unified.util.registry.RegistryResourceListener;

public class EntityRegistry extends RegistryResourceListener<EntityRegistry.Definition> {
    public static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.ENTITY_TYPE).fieldOf("base").forGetter(Definition::base),
            Codec.either(Identifier.CODEC, MobVariants.Variant.CODEC).fieldOf("default_variant").forGetter(Definition::defaultVariant)
    ).apply(instance, Definition::new));

    public EntityRegistry() {
        super(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "entities"), CODEC);
    }

    @Override
    protected void register(Identifier id, Definition definition) {
        InternalHandlerImpl.INSTANCE.impl().registerEntityCopy(id, definition.base(), definition.defaultVariant());
    }

    public record Definition(ResourceKey<EntityType<?>> base, Either<Identifier, MobVariants.Variant> defaultVariant) {}
}
