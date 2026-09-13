package net.rebel459.unified.api.registry;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.codec.UnifiedCodecs;

import java.util.function.Function;
import java.util.function.Predicate;

public final class UnifiedMapColorTypes {
    public static final ExtensibleCodec.Complex<Function<BlockState, MapColor>,
            UnifiedCodecs.Conditional<Predicate<BlockState>, Function<BlockState, MapColor>>> CONDITIONAL = ExtensibleCodecs.MAP_COLOR_TYPES.register(
            Unified.id("conditional"),
            UnifiedCodecs.conditional(ExtensibleCodecs.PREDICATE_TYPES, ExtensibleCodecs.MAP_COLOR_TYPES),
            definition -> {
                Predicate<BlockState> predicate = definition.predicate().get();
                Function<BlockState, MapColor> ifTrue = definition.ifTrue().get();
                Function<BlockState, MapColor> ifFalse = definition.ifFalse().get();
                return state -> predicate.test(state) ? ifTrue.apply(state) : ifFalse.apply(state);
            }
    );

    private UnifiedMapColorTypes() {}

    public static void init() {}
}
