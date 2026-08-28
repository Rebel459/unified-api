package net.rebel459.unified.util.codec;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Function;
import java.util.function.ToIntFunction;

public class ExtensibleCodecs {
    public static final ExtensibleItemCodec ITEM_TYPES = new ExtensibleItemCodec("type");
    public static final ExtensibleBlockCodec BLOCK_TYPES = new ExtensibleBlockCodec("type");
    public static final ExtensibleCodec<BlockBehaviour.StatePredicate> STATE_PREDICATE_TYPES = new ExtensibleCodec<>("type");
    public static final ExtensibleCodec<BlockBehaviour.StateArgumentPredicate<EntityType<?>>> ENTITY_STATE_PREDICATE_TYPES = new ExtensibleCodec<>("type");
    public static final ExtensibleCodec<Function<BlockState, MapColor>> MAP_COLOR_TYPES = new ExtensibleCodec<>("type");
    public static final ExtensibleCodec<ToIntFunction<BlockState>> LIGHT_EMISSION_TYPES = new ExtensibleCodec<>("type");
    public static final ExtensibleCodec<BlockBehaviour.PostProcess> POST_PROCESS_TYPES = new ExtensibleCodec<>("type");

    public static void init() {}
}
