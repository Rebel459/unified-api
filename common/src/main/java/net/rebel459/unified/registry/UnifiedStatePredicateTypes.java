package net.rebel459.unified.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.rebel459.unified.Unified;
import net.rebel459.unified.util.codec.ExtensibleCodec;
import net.rebel459.unified.util.codec.ExtensibleCodecs;

import java.util.function.Function;

public class UnifiedStatePredicateTypes {

    private static ExtensibleCodec.Simple<BlockBehaviour.StatePredicate> simple(String path, BlockBehaviour.StatePredicate predicate) {
        Identifier id = Identifier.fromNamespaceAndPath(Unified.MOD_ID, path);
        ExtensibleCodecs.ENTITY_STATE_PREDICATE_TYPES.register(id, () -> (state, getter, pos, _) -> predicate.test(state, getter, pos));
        return ExtensibleCodecs.STATE_PREDICATE_TYPES.register(id, () -> predicate);
    }

    private static <T> ExtensibleCodec.Complex<BlockBehaviour.StatePredicate, T> complex(String path, MapCodec<T> codec, Function<T, BlockBehaviour.StatePredicate> predicate) {
        Identifier id = Identifier.fromNamespaceAndPath(Unified.MOD_ID, path);
        ExtensibleCodecs.ENTITY_STATE_PREDICATE_TYPES.register(id, codec, definition -> (state, getter, pos, _) -> predicate.apply(definition).test(state, getter, pos));
        return ExtensibleCodecs.STATE_PREDICATE_TYPES.register(id, codec, predicate);
    }

    public static final ExtensibleCodec.Complex<BlockBehaviour.StateArgumentPredicate<EntityType<?>>, HolderSet<EntityType<?>>> ENTITY_MATCHES = ExtensibleCodecs.ENTITY_STATE_PREDICATE_TYPES.register(
            Identifier.fromNamespaceAndPath(Unified.MOD_ID, "entity_matches"),
            RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).fieldOf("entities"),
            definition -> (_, _, _, entity) -> definition.stream().anyMatch(holder -> holder.value() == entity)
    );

    public static void init() {}
}

