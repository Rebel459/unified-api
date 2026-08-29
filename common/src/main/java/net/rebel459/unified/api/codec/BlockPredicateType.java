package net.rebel459.unified.api.codec;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BlockPredicateType {
    public interface Simple {
        Optional<ExtensibleCodec.Simple<Predicate<BlockState>>> predicate();
        Optional<ExtensibleCodec.Simple<BlockBehaviour.StatePredicate>> statePredicate();
        Optional<ExtensibleCodec.Simple<BlockBehaviour.StateArgumentPredicate<EntityType<?>>>> entityPredicate();
        Optional<ExtensibleCodec.Simple<BlockBehaviour.StateArgumentPredicate<AABB>>> collisionPredicate();
    }

    public interface Complex<T> {
        Optional<ExtensibleCodec.Complex<Predicate<BlockState>, T>> predicate();
        Optional<ExtensibleCodec.Complex<BlockBehaviour.StatePredicate, T>> statePredicate();
        Optional<ExtensibleCodec.Complex<BlockBehaviour.StateArgumentPredicate<EntityType<?>>, T>> entityPredicate();
        Optional<ExtensibleCodec.Complex<BlockBehaviour.StateArgumentPredicate<AABB>, T>> collisionPredicate();
    }

    public static BlockPredicateType.Simple register(Identifier id, Optional<Supplier<Predicate<BlockState>>> predicate, Optional<Supplier<BlockBehaviour.StatePredicate>> statePredicate, Optional<Supplier<BlockBehaviour.StateArgumentPredicate<EntityType<?>>>> entityPredicate, Optional<Supplier<BlockBehaviour.StateArgumentPredicate<AABB>>> collisionPredicate) {
        Optional<ExtensibleCodec.Simple<Predicate<BlockState>>> registeredPredicate;
        Optional<ExtensibleCodec.Simple<BlockBehaviour.StatePredicate>> registeredStatePredicate;
        Optional<ExtensibleCodec.Simple<BlockBehaviour.StateArgumentPredicate<EntityType<?>>>> registeredEntityPredicate;
        Optional<ExtensibleCodec.Simple<BlockBehaviour.StateArgumentPredicate<AABB>>> registeredCollisionPredicate;
        if (predicate.isPresent()) registeredPredicate = Optional.of(ExtensibleCodecs.PREDICATE_TYPES.register(id, predicate.get()));
        else registeredPredicate = Optional.empty();
        if (statePredicate.isPresent()) registeredStatePredicate = Optional.of(ExtensibleCodecs.STATE_PREDICATE_TYPES.register(id, statePredicate.get()));
        else registeredStatePredicate = Optional.empty();
        if (entityPredicate.isPresent()) registeredEntityPredicate = Optional.of(ExtensibleCodecs.ENTITY_PREDICATE_TYPES.register(id, entityPredicate.get()));
        else registeredEntityPredicate = Optional.empty();
        if (collisionPredicate.isPresent()) registeredCollisionPredicate = Optional.of(ExtensibleCodecs.COLLISION_PREDICATE_TYPES.register(id, collisionPredicate.get()));
        else registeredCollisionPredicate = Optional.empty();
        return new BlockPredicateType.Simple() {
            @Override
            public Optional<ExtensibleCodec.Simple<Predicate<BlockState>>> predicate() {
                return registeredPredicate;
            }

            @Override
            public Optional<ExtensibleCodec.Simple<BlockBehaviour.StatePredicate>> statePredicate() {
                return registeredStatePredicate;
            }

            @Override
            public Optional<ExtensibleCodec.Simple<BlockBehaviour.StateArgumentPredicate<EntityType<?>>>> entityPredicate() {
                return registeredEntityPredicate;
            }

            @Override
            public Optional<ExtensibleCodec.Simple<BlockBehaviour.StateArgumentPredicate<AABB>>> collisionPredicate() {
                return registeredCollisionPredicate;
            }
        };
    }

    public static <T> BlockPredicateType.Complex<T> register(Identifier id, MapCodec<T> codec, Optional<Function<T, Predicate<BlockState>>> predicate, Optional<Function<T, BlockBehaviour.StatePredicate>> statePredicate, Optional<Function<T, BlockBehaviour.StateArgumentPredicate<EntityType<?>>>> entityPredicate, Optional<Function<T, BlockBehaviour.StateArgumentPredicate<AABB>>> collisionPredicate) {
        Optional<ExtensibleCodec.Complex<Predicate<BlockState>, T>> registeredPredicate;
        Optional<ExtensibleCodec.Complex<BlockBehaviour.StatePredicate, T>> registeredStatePredicate;
        Optional<ExtensibleCodec.Complex<BlockBehaviour.StateArgumentPredicate<EntityType<?>>, T>> registeredEntityPredicate;
        Optional<ExtensibleCodec.Complex<BlockBehaviour.StateArgumentPredicate<AABB>, T>> registeredCollisionPredicate;
        if (predicate.isPresent()) registeredPredicate = Optional.of(ExtensibleCodecs.PREDICATE_TYPES.register(id, codec, predicate.get()));
        else registeredPredicate = Optional.empty();
        if (statePredicate.isPresent()) registeredStatePredicate = Optional.of(ExtensibleCodecs.STATE_PREDICATE_TYPES.register(id, codec, statePredicate.get()));
        else registeredStatePredicate = Optional.empty();
        if (entityPredicate.isPresent()) registeredEntityPredicate = Optional.of(ExtensibleCodecs.ENTITY_PREDICATE_TYPES.register(id, codec, entityPredicate.get()));
        else registeredEntityPredicate = Optional.empty();
        if (collisionPredicate.isPresent()) registeredCollisionPredicate = Optional.of(ExtensibleCodecs.COLLISION_PREDICATE_TYPES.register(id, codec, collisionPredicate.get()));
        else registeredCollisionPredicate = Optional.empty();
        return new BlockPredicateType.Complex<>() {
            @Override
            public Optional<ExtensibleCodec.Complex<Predicate<BlockState>, T>> predicate() {
                return registeredPredicate;
            }

            @Override
            public Optional<ExtensibleCodec.Complex<BlockBehaviour.StatePredicate, T>> statePredicate() {
                return registeredStatePredicate;
            }

            @Override
            public Optional<ExtensibleCodec.Complex<BlockBehaviour.StateArgumentPredicate<EntityType<?>>, T>> entityPredicate() {
                return registeredEntityPredicate;
            }

            @Override
            public Optional<ExtensibleCodec.Complex<BlockBehaviour.StateArgumentPredicate<AABB>, T>> collisionPredicate() {
                return registeredCollisionPredicate;
            }
        };
    }
}
