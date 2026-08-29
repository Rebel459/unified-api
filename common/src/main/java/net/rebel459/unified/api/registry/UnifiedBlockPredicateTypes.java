package net.rebel459.unified.api.registry;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.codec.BlockPredicateType;

import java.util.Optional;

public class UnifiedBlockPredicateTypes {

    public static final BlockPredicateType.Complex<HolderSet<EntityType<?>>> ENTITY_MATCHES = BlockPredicateType.register(
            Identifier.fromNamespaceAndPath(Unified.MOD_ID, "entity_matches"),
            RegistryCodecs.holderSet(Registries.ENTITY_TYPE).fieldOf("entities"),
            Optional.empty(),
            Optional.empty(),
            Optional.of(definition -> (_, _, _, entity) -> definition.stream().anyMatch(holder -> holder.value() == entity)),
            Optional.empty()
    );

    public static void init() {}
}

