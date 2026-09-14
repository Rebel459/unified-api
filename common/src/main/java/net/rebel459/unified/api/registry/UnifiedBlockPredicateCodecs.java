package net.rebel459.unified.api.registry;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.world.entity.EntityType;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.codec.ExtensibleBlockPredicateCodec;

import java.util.Optional;

public class UnifiedBlockPredicateCodecs {

    public static final ExtensibleBlockPredicateCodec.Complex<HolderSet<EntityType<?>>> ENTITY_MATCHES = ExtensibleBlockPredicateCodec.register(
            Unified.id("entity_matches"),
            RegistryCodecs.holderSet(Registries.ENTITY_TYPE).fieldOf("entities"),
            Optional.empty(),
            Optional.empty(),
            Optional.of(definition -> (_, _, _, entity) -> definition.stream().anyMatch(holder -> holder.value() == entity)),
            Optional.empty()
    );

    public static void init() {}
}

