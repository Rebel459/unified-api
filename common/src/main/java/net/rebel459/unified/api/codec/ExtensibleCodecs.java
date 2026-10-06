package net.rebel459.unified.api.codec;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.item.CreativeModeTab;
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

    public static final ExtensibleItemCodec ITEM = new ExtensibleItemCodec();

    public static final ExtensibleBlockItemCodec BLOCK_ITEM = new ExtensibleBlockItemCodec();

    public static final ExtensibleBlockCodec BLOCK = new ExtensibleBlockCodec();

    public static final ExtensibleEntityCodec ENTITY = new ExtensibleEntityCodec();

    public static final ExtensiblePredicateCodec<Predicate<BlockState>> BLOCK_PREDICATE = ExtensiblePredicateCodec.predicate();

    public static final ExtensiblePredicateCodec<Predicate<ItemStack>> ITEM_PREDICATE = ExtensiblePredicateCodec.predicate();

    public static final ExtensiblePredicateCodec<BlockBehaviour.StatePredicate> STATE_PREDICATE = new ExtensiblePredicateCodec<>(evaluation ->
            (state, level, pos) -> evaluation.evaluate(predicate -> predicate.test(state, level, pos)));

    public static final ExtensiblePredicateCodec<BlockBehaviour.StateArgumentPredicate<EntityType<?>>> ENTITY_PREDICATE = new ExtensiblePredicateCodec<>(evaluation ->
            (state, level, pos, argument) -> evaluation.evaluate(predicate -> predicate.test(state, level, pos, argument)));

    public static final ExtensiblePredicateCodec<BlockBehaviour.StateArgumentPredicate<AABB>> COLLISION_PREDICATE = new ExtensiblePredicateCodec<>(evaluation ->
            (state, level, pos, argument) -> evaluation.evaluate(predicate -> predicate.test(state, level, pos, argument)));

    public static final ExtensibleCodec<Function<BlockState, MapColor>> MAP_COLOR = new ExtensibleCodec<>();

    public static final ExtensibleCodec<ToIntFunction<BlockState>> LIGHT_EMISSION = new ExtensibleCodec<>();

    public static final ExtensibleCodec<BlockBehaviour.PostProcess> POST_PROCESS = new ExtensibleCodec<>();

    public static final ExtensibleCodec<Consumer<UseOnContext>> USE_CONTEXT = new ExtensibleCodec<>();

    public static final ExtensiblePredicateCodec<Boolean> LOAD_REQUIREMENT = new ExtensiblePredicateCodec<>(evaluation -> evaluation.evaluate(Boolean::booleanValue));

    public static final ExtensibleCodec<SpawnPlacementType> SPAWN_PLACEMENT = new ExtensibleCodec<>();

    public static final ExtensibleSpawnPredicate SPAWN_PREDICATE = new ExtensibleSpawnPredicate();

    public static final ExtensibleCodec<CreativeModeTab.DisplayItemsGenerator> DISPLAY_ITEMS = new ExtensibleCodec<>();

    public static void init() {}
}
