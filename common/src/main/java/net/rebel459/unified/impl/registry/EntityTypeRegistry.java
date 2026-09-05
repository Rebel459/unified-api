package net.rebel459.unified.impl.registry;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.core.RegistryResourceListener;
import net.rebel459.unified.impl.data.MobVariants;
import net.rebel459.unified.impl.platform.PlatformHandler;

public class EntityTypeRegistry extends RegistryResourceListener<EntityTypeRegistry.Definition> {
    public static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.ENTITY_TYPE).fieldOf("base").forGetter(Definition::base),
            Codec.either(Identifier.CODEC, MobVariants.Variant.CODEC).fieldOf("default_variant").forGetter(Definition::defaultVariant)
    ).apply(instance, Definition::new));

    public static final Identifier ID = Unified.id("entity_types");

    public EntityTypeRegistry() {
        super(ID, CODEC, SoundEventRegistry.ID);
    }

    @Override
    protected void register(Identifier id, DeferredDeclaration<Definition> declaration) {
        Definition definition = declaration.get();
        PlatformHandler.INSTANCE.internal().registerEntityCopy(id, definition.base(), definition.defaultVariant());
    }

    public record Definition(ResourceKey<EntityType<?>> base, Either<Identifier, MobVariants.Variant> defaultVariant) {}
}
