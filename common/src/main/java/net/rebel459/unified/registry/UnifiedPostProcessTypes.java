package net.rebel459.unified.registry;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.rebel459.unified.util.codec.ExtensibleCodec;
import net.rebel459.unified.util.codec.ExtensibleCodecs;

import java.util.function.Function;

public class UnifiedPostProcessTypes {

    private static ExtensibleCodec.Simple<BlockBehaviour.PostProcess> simple(String path, BlockBehaviour.PostProcess predicate) {
        return ExtensibleCodecs.POST_PROCESS_TYPES.register(Identifier.withDefaultNamespace(path), () -> predicate);
    }

    private static <T> ExtensibleCodec.Complex<BlockBehaviour.PostProcess, T> complex(String path, MapCodec<T> codec, Function<T, BlockBehaviour.PostProcess> predicate) {
        return ExtensibleCodecs.POST_PROCESS_TYPES.register(Identifier.withDefaultNamespace(path), codec, predicate);
    }

    public static final ExtensibleCodec.Complex<BlockBehaviour.PostProcess, Offset> OFFSET = complex(
            "offset",
            Offset.CODEC,
            definition -> (_, _, pos) -> pos.relative(definition.direction, definition.distance)
    );

    public record Offset(Direction direction, int distance) {
        public static final MapCodec<Offset> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Direction.CODEC.fieldOf("direction").forGetter(Offset::direction),
                ExtraCodecs.POSITIVE_INT.optionalFieldOf("distance", 1).forGetter(Offset::distance)
        ).apply(instance, Offset::new));
    }

    public static void init() {}
}

