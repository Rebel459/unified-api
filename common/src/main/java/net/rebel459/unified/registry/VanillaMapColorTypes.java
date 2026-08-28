package net.rebel459.unified.registry;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.rebel459.unified.util.codec.CodecUtils;
import net.rebel459.unified.util.codec.ExtensibleCodec;
import net.rebel459.unified.util.codec.ExtensibleCodecs;

import java.util.function.Function;

public class VanillaMapColorTypes {

    private static final MapCodec<MapColor> COLOR = CodecUtils.named(MapColor.class).fieldOf("color");

    private static ExtensibleCodec.Simple<Function<BlockState, MapColor>> simple(String path, Function<BlockState, MapColor> predicate) {
        return ExtensibleCodecs.MAP_COLOR_TYPES.register(Identifier.withDefaultNamespace(path), () -> predicate);
    }

    private static <T> ExtensibleCodec.Complex<Function<BlockState, MapColor>, T> complex(String path, MapCodec<T> codec, Function<T, Function<BlockState, MapColor>> predicate) {
        return ExtensibleCodecs.MAP_COLOR_TYPES.register(Identifier.withDefaultNamespace(path), codec, predicate);
    }

    public static final ExtensibleCodec.Simple<Function<BlockState, MapColor>> WATERLOGGED = simple("waterlogged", state -> state.getValue(BlockStateProperties.WATERLOGGED) ? MapColor.WATER : MapColor.NONE);

    public static final ExtensibleCodec.Complex<Function<BlockState, MapColor>, MapColor> SIMPLE = complex(
            "simple",
            COLOR,
            definition -> _ -> definition
    );

    public static final ExtensibleCodec.Complex<Function<BlockState, MapColor>, DyeColor> BED = complex(
            "bed",
            CodecUtils.named(DyeColor.class).fieldOf("dye_color"),
            definition -> state -> state.getValue(BedBlock.PART) == BedPart.FOOT ? definition.getMapColor() : MapColor.WOOL
    );

    public static final ExtensibleCodec.Complex<Function<BlockState, MapColor>, MultiColored> BLOCK_ROTATION = complex(
            "block_rotation",
            MultiColored.codec("top_color", "side_color"),
            definition -> state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? definition.primary : definition.secondary
    );

    public static final ExtensibleCodec.Complex<Function<BlockState, MapColor>, CropAge> CROP_AGE = complex(
            "crop_age",
            CropAge.CODEC,
            definition -> state -> state.getValue(CropBlock.AGE) >= definition.age ? definition.color : MapColor.PLANT
    );

    public record CropAge(int age, MapColor color){
        public static MapCodec<CropAge> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("age").forGetter(CropAge::age),
                COLOR.forGetter(CropAge::color)
        ).apply(instance, CropAge::new));
    }

    public record MultiColored(MapColor primary, MapColor secondary){
        public static MapCodec<MultiColored> codec(String primaryField, String secondaryField) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    CodecUtils.named(MapColor.class).fieldOf(primaryField).forGetter(MultiColored::primary),
                    CodecUtils.named(MapColor.class).fieldOf(secondaryField).forGetter(MultiColored::secondary)
            ).apply(instance, MultiColored::new));
        }
    }

    public static void init() {}
}

