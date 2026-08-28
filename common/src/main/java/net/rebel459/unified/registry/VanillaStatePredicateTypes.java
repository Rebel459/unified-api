package net.rebel459.unified.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.rebel459.unified.util.codec.ExtensibleCodec;
import net.rebel459.unified.util.codec.ExtensibleCodecs;

import java.util.function.Function;

public class VanillaStatePredicateTypes {

    private static ExtensibleCodec.Simple<BlockBehaviour.StatePredicate> simple(String path, BlockBehaviour.StatePredicate predicate) {
        Identifier id = Identifier.withDefaultNamespace(path);
        ExtensibleCodecs.ENTITY_STATE_PREDICATE_TYPES.register(id, () -> (state, getter, pos, _) -> predicate.test(state, getter, pos));
        return ExtensibleCodecs.STATE_PREDICATE_TYPES.register(id, () -> predicate);
    }

    private static <T> ExtensibleCodec.Complex<BlockBehaviour.StatePredicate, T> complex(String path, MapCodec<T> codec, Function<T, BlockBehaviour.StatePredicate> predicate) {
        Identifier id = Identifier.withDefaultNamespace(path);
        ExtensibleCodecs.ENTITY_STATE_PREDICATE_TYPES.register(
                id,
                codec, definition -> (state, getter, pos, _) -> predicate.apply(definition).test(state, getter, pos)
        );
        return ExtensibleCodecs.STATE_PREDICATE_TYPES.register(id, codec, predicate);
    }

    public static final ExtensibleCodec.Simple<BlockBehaviour.StatePredicate> NEVER = simple("never", (state, getter, pos) -> false);
    public static final ExtensibleCodec.Simple<BlockBehaviour.StatePredicate> ALWAYS = simple("always", (state, getter, pos) -> true);
    public static final ExtensibleCodec.Simple<BlockBehaviour.StatePredicate> NOT_CLOSED_SHULKER = simple("not_closed_shulker", Blocks.NOT_CLOSED_SHULKER);
    public static final ExtensibleCodec.Simple<BlockBehaviour.StatePredicate> NOT_EXTENDED_PISTON = simple("not_extended_piston", Blocks.NOT_EXTENDED_PISTON);

    public static final ExtensibleCodec.Simple<BlockBehaviour.StateArgumentPredicate<EntityType<?>>> DEFAULT = ExtensibleCodecs.ENTITY_STATE_PREDICATE_TYPES.register(
            Identifier.withDefaultNamespace("default"),
            () -> (state, level, pos, _) -> state.isFaceSturdy(level, pos, Direction.UP) && state.getLightEmission() < 14
    );

    public static final ExtensibleCodec.Simple<BlockBehaviour.StateArgumentPredicate<EntityType<?>>> OCELOT_OR_PARROT = ExtensibleCodecs.ENTITY_STATE_PREDICATE_TYPES.register(
            Identifier.withDefaultNamespace("ocelot_or_parrot"),
            () -> Blocks::ocelotOrParrot
    );

    public static final ExtensibleCodec.Simple<BlockBehaviour.StateArgumentPredicate<EntityType<?>>> POLAR_BEAR = ExtensibleCodecs.ENTITY_STATE_PREDICATE_TYPES.register(
            Identifier.withDefaultNamespace("polar_bear"),
            () -> (_, _, _, entity) -> entity == EntityType.POLAR_BEAR
    );

    public static void init() {}
}
