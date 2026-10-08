package net.rebel459.unified.api.registry;

import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.rebel459.unified.api.codec.ExtensibleBlockPredicateCodec;

import java.util.Optional;
import java.util.function.Predicate;

public class VanillaBlockPredicateCodecs {

    private static ExtensibleBlockPredicateCodec.Simple simple(String path, Predicate<BlockState> predicate) {
        return ExtensibleBlockPredicateCodec.register(Identifier.withDefaultNamespace(path), Optional.of(() -> predicate), Optional.of(() -> (state, _, _) -> predicate.test(state)), Optional.of((() -> (state, _, _, _) -> predicate.test(state))), Optional.empty());
    }
    private static ExtensibleBlockPredicateCodec.Simple simple(String path, BlockBehaviour.StatePredicate predicate) {
        return ExtensibleBlockPredicateCodec.register(Identifier.withDefaultNamespace(path), Optional.empty(), Optional.of(() -> predicate), Optional.of((() -> (state, getter, pos, _) -> predicate.test(state, getter, pos))), Optional.empty());
    }
    private static ExtensibleBlockPredicateCodec.Simple simpleEntity(String path, BlockBehaviour.StateArgumentPredicate<EntityType<?>> predicate) {
        return ExtensibleBlockPredicateCodec.register(Identifier.withDefaultNamespace(path), Optional.empty(), Optional.empty(), Optional.of((() -> predicate)), Optional.empty());
    }
    private static ExtensibleBlockPredicateCodec.Simple simpleCollision(String path, BlockBehaviour.StateArgumentPredicate<AABB> predicate) {
        return ExtensibleBlockPredicateCodec.register(Identifier.withDefaultNamespace(path), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(() -> predicate));
    }

    public static final ExtensibleBlockPredicateCodec.Simple NOT_CLOSED_SHULKER = simple("not_closed_shulker", Blocks.NOT_CLOSED_SHULKER);
    public static final ExtensibleBlockPredicateCodec.Simple NOT_EXTENDED_PISTON = simple("not_extended_piston", Blocks.NOT_EXTENDED_PISTON);
    public static final ExtensibleBlockPredicateCodec.Simple NEAR_PLANE_INTERSECTS_OUTLINE = simpleCollision("near_plane_intersects_outline", Blocks.NEAR_PLANE_INTERSECTS_OUTLINE);
    public static final ExtensibleBlockPredicateCodec.Simple DEFAULT = simple("default", (state, level, pos) -> state.isFaceSturdy(level, pos, Direction.UP) && state.getLightEmission() < 14);
    public static final ExtensibleBlockPredicateCodec.Simple OCELOT_OR_PARROT = simpleEntity("ocelot_or_parrot", Blocks::ocelotOrParrot);
    public static final ExtensibleBlockPredicateCodec.Simple POLAR_BEAR = simpleEntity("polar_bear", (_, _, _, entity) -> entity == EntityTypes.POLAR_BEAR);

    public static void init() {}
}
