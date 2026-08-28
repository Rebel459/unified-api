package net.rebel459.unified.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.rebel459.unified.util.codec.ExtensibleCodec;
import net.rebel459.unified.util.codec.ExtensibleCodecs;

import java.util.function.Function;
import java.util.function.ToIntFunction;

public class VanillaLightEmissionTypes {

    private static final MapCodec<Integer> VALUE = ExtraCodecs.NON_NEGATIVE_INT.fieldOf("value");

    private static ExtensibleCodec.Simple<ToIntFunction<BlockState>> simple(String path, ToIntFunction<BlockState> predicate) {
        return ExtensibleCodecs.LIGHT_EMISSION_TYPES.register(Identifier.withDefaultNamespace(path), () -> predicate);
    }

    private static <T> ExtensibleCodec.Complex<ToIntFunction<BlockState>, T> complex(String path, MapCodec<T> codec, Function<T, ToIntFunction<BlockState>> predicate) {
        return ExtensibleCodecs.LIGHT_EMISSION_TYPES.register(Identifier.withDefaultNamespace(path), codec, predicate);
    }

    public static final ExtensibleCodec.Simple<ToIntFunction<BlockState>> SEA_PICKLE = simple("sea_pickle", state -> SeaPickleBlock.isDead(state) ? 0 : 3 + 3 * state.getValue(SeaPickleBlock.PICKLES));
    public static final ExtensibleCodec.Simple<ToIntFunction<BlockState>> LIGHT_BLOCK = simple("light_block", LightBlock.LIGHT_EMISSION);

    public static final ExtensibleCodec.Complex<ToIntFunction<BlockState>, Integer> SIMPLE = complex(
            "simple",
            VALUE,
            definition -> _ -> definition
    );

    public static final ExtensibleCodec.Complex<ToIntFunction<BlockState>, Integer> WHEN_LIT = complex(
            "when_lit",
            VALUE,
            definition -> state -> state.getValue(BlockStateProperties.LIT) ? definition : 0
    );

    public static final ExtensibleCodec.Complex<ToIntFunction<BlockState>, Integer> GLOW_LICHEN = complex(
            "glow_lichen",
            VALUE,
            GlowLichenBlock::emission
    );

    public static final ExtensibleCodec.Complex<ToIntFunction<BlockState>, Integer> RESPAWN_ANCHOR = complex(
            "respawn_anchor",
            VALUE,
            definition -> state -> RespawnAnchorBlock.getScaledChargeLevel(state, definition)
    );

    public static void init() {}
}

