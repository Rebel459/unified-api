package net.rebel459.unified.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.rebel459.unified.util.codec.ExtensibleCodec;
import net.rebel459.unified.util.codec.ExtensibleCodecs;

import java.util.function.Function;

public class VanillaPostProcessTypes {

    private static ExtensibleCodec.Simple<BlockBehaviour.PostProcess> simple(String path, BlockBehaviour.PostProcess predicate) {
        return ExtensibleCodecs.POST_PROCESS_TYPES.register(Identifier.withDefaultNamespace(path), () -> predicate);
    }

    private static <T> ExtensibleCodec.Complex<BlockBehaviour.PostProcess, T> complex(String path, MapCodec<T> codec, Function<T, BlockBehaviour.PostProcess> predicate) {
        return ExtensibleCodecs.POST_PROCESS_TYPES.register(Identifier.withDefaultNamespace(path), codec, predicate);
    }

    public static final ExtensibleCodec.Simple<BlockBehaviour.PostProcess> PROCESS_SELF = simple("process_self", (state, getter, pos) -> pos);
    public static final ExtensibleCodec.Simple<BlockBehaviour.PostProcess> PROCESS_ABOVE = simple("process_above", (state, getter, pos) -> pos.above());

    public static void init() {}
}
