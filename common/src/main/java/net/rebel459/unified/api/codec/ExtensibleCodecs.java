package net.rebel459.unified.api.codec;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class ExtensibleCodecs {
    public static final ItemType ITEM_TYPES = new ItemType("type");
    public static final BlockType BLOCK_TYPES = new BlockType("type");
    public static final ExtensibleCodec<Predicate<BlockState>> PREDICATE_TYPES = new ExtensibleCodec<>("type");
    public static final ExtensibleCodec<BlockBehaviour.StatePredicate> STATE_PREDICATE_TYPES = new ExtensibleCodec<>("type");
    public static final ExtensibleCodec<BlockBehaviour.StateArgumentPredicate<EntityType<?>>> ENTITY_PREDICATE_TYPES = new ExtensibleCodec<>("type");
    public static final ExtensibleCodec<BlockBehaviour.StateArgumentPredicate<AABB>> COLLISION_PREDICATE_TYPES = new ExtensibleCodec<>("type");
    public static final ExtensibleCodec<Function<BlockState, MapColor>> MAP_COLOR_TYPES = new ExtensibleCodec<>("type");
    public static final ExtensibleCodec<ToIntFunction<BlockState>> LIGHT_EMISSION_TYPES = new ExtensibleCodec<>("type");
    public static final ExtensibleCodec<BlockBehaviour.PostProcess> POST_PROCESS_TYPES = new ExtensibleCodec<>("type");

    public static void init() {}
}
