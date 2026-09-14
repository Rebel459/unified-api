package net.rebel459.unified.api.codec;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ServerLevelAccessor;

import java.util.function.Supplier;

public final class ExtensibleSpawnPredicate extends ExtensiblePredicateCodec<ExtensibleSpawnPredicate.Type> {

    public ExtensibleSpawnPredicate() {
        super(evaluation -> (entity, level, reason, pos, random) -> evaluation.evaluate(predicate -> predicate.test(entity, level, reason, pos, random)));
    }

    public <T extends Entity> Simple<Type> registerSpawnPredicate(Identifier id, Supplier<SpawnPlacements.SpawnPredicate<T>> supplier) {
        return register(id, () -> wrap(supplier.get()));
    }

    private static <T extends Entity> Type wrap(SpawnPlacements.SpawnPredicate<T> predicate) {
        return (entity, level, reason, pos, random) -> predicate.test(
                (EntityType<T>) entity,
                level,
                reason,
                pos,
                random
        );
    }

    @FunctionalInterface
    public interface Type {
        boolean test(EntityType<?> entityType, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random);
    }
}