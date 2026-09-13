package net.rebel459.unified.api.codec;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class ExtensibleCodecs {
    public static final ItemType ITEM_TYPES = new ItemType();
    public static final BlockItemType BLOCK_ITEM_TYPES = new BlockItemType();
    public static final BlockType BLOCK_TYPES = new BlockType();
    public static final PredicateType<Predicate<BlockState>> PREDICATE_TYPES = PredicateType.predicate();
    public static final PredicateType<Predicate<ItemStack>> ITEM_PREDICATE_TYPES = PredicateType.predicate();
    public static final PredicateType<BlockBehaviour.StatePredicate> STATE_PREDICATE_TYPES = new PredicateType<>(evaluation ->
            (state, level, pos) -> evaluation.evaluate(predicate -> predicate.test(state, level, pos)));
    public static final PredicateType<BlockBehaviour.StateArgumentPredicate<EntityType<?>>> ENTITY_PREDICATE_TYPES = new PredicateType<>(evaluation ->
            (state, level, pos, argument) -> evaluation.evaluate(predicate -> predicate.test(state, level, pos, argument)));
    public static final PredicateType<BlockBehaviour.StateArgumentPredicate<AABB>> COLLISION_PREDICATE_TYPES = new PredicateType<>(evaluation ->
            (state, level, pos, argument) -> evaluation.evaluate(predicate -> predicate.test(state, level, pos, argument)));

    public static final ExtensibleCodec<Function<BlockState, MapColor>> MAP_COLOR_TYPES = new ExtensibleCodec<>();
    public static final ExtensibleCodec<ToIntFunction<BlockState>> LIGHT_EMISSION_TYPES = new ExtensibleCodec<>();
    public static final ExtensibleCodec<BlockBehaviour.PostProcess> POST_PROCESS_TYPES = new ExtensibleCodec<>();
    public static final ExtensibleCodec<Consumer<UseOnContext>> USE_CONTEXT_TYPES = new ExtensibleCodec<>();
    public static final PredicateType<Boolean> REQUIREMENT_TYPES = new PredicateType<>(evaluation -> evaluation.evaluate(Boolean::booleanValue));

    public static void init() {}
}
