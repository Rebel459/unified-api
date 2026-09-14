package net.rebel459.unified.api.registry;

import net.minecraft.world.level.block.state.BlockState;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.codec.UnifiedCodecs;

import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public final class UnifiedLightEmissionCodecs {
    public static final ExtensibleCodec.Complex<ToIntFunction<BlockState>,
            UnifiedCodecs.Conditional<Predicate<BlockState>, ToIntFunction<BlockState>>> CONDITIONAL = ExtensibleCodecs.LIGHT_EMISSION.register(
            Unified.id("conditional"),
            UnifiedCodecs.conditional(ExtensibleCodecs.BLOCK_PREDICATE, ExtensibleCodecs.LIGHT_EMISSION),
            definition -> {
                Predicate<BlockState> predicate = definition.predicate().get();
                ToIntFunction<BlockState> ifTrue = definition.ifTrue().get();
                ToIntFunction<BlockState> ifFalse = definition.ifFalse().get();
                return state -> predicate.test(state) ? ifTrue.applyAsInt(state) : ifFalse.applyAsInt(state);
            }
    );

    private UnifiedLightEmissionCodecs() {}

    public static void init() {}
}
