package net.rebel459.unified.api.asset;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.util.BlockLike;

public final class BlockAssets {

    public static final BlockAsset<Void> SIMPLE_CUBE = create("simple_cube");
    public static final BlockAsset<Void> LEAVES = create("leaves");
    public static final BlockAsset<Void> LOG = create("log");
    public static final BlockAsset<Void> LOG_UV_LOCKED = create("log_uv_locked");
    public static final BlockAsset<Void> LANTERN = create("lantern");
    public static final BlockAsset<Void> DOOR = create("door");
    public static final BlockAsset<Void> TRAPDOOR = create("trapdoor");
    public static final BlockAsset<Void> CHAIN = create("chain");
    public static final BlockAsset<Void> TINTED_DOUBLE_PLANT = create("tinted_double_plant");
    public static final BlockAsset<Void> PARTICLE_ONLY = create("particle_only");
    public static final BlockAsset<Void> ROTATED_PILLAR = create("rotated_pillar");

    public static final BlockAsset<Integer> TINTED_LEAVES = create("tinted_leaves");
    public static final BlockAsset<BlockLike> SLAB = create("slab");
    public static final BlockAsset<BlockLike> STAIRS = create("stairs");
    public static final BlockAsset<BlockLike> WALL = create("wall");
    public static final BlockAsset<PlantType> PLANT = create("plant");
    public static final BlockAsset<PlantType> DOUBLE_PLANT = create("double_plant");
    public static final BlockAsset<PottedPlant> POTTED_PLANT = create("potted_plant");
    public static final BlockAsset<Crop> CROP = create("crop");
    public static final BlockAsset<BlockLike> COPIED_PARTICLE_ONLY = create("copied_particle_only");
    public static final BlockAsset<BlockLike> WOOD = create("wood");
    public static final BlockAsset<HangingSign> HANGING_SIGN = create("hanging_sign");
    public static final BlockAsset<BlockLike> SHELF = create("shelf");
    public static final BlockAsset<BlockLike> BUTTON = create("button");
    public static final BlockAsset<BlockLike> FENCE = create("fence");
    public static final BlockAsset<BlockLike> FENCE_GATE = create("fence_gate");
    public static final BlockAsset<BlockLike> PRESSURE_PLATE = create("pressure_plate");
    public static final BlockAsset<Sign> SIGN = create("sign");

    public record Crop(Property<Integer> property, int stages) {}

    public record HangingSign(BlockLike strippedLog, BlockLike wallHangingSign) {}

    public record Sign(BlockLike base, BlockLike wallSign) {}

    public record PottedPlant(BlockLike potted, PlantType type) {}

    public enum PlantType {
        TINTED,
        NOT_TINTED,
        EMISSIVE_NOT_TINTED
    }

    private BlockAssets() {}

    private static <T> BlockAsset<T> create(String path) {
        return new BlockAsset<>(Unified.id(path));
    }
}
