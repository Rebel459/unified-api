package net.rebel459.unified.api.registry;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.core.Supplied;
import net.rebel459.unified.api.core.UnifiedRegistries;

public class UnifiedDataComponents {

    public static void init() {}

    private static final UnifiedRegistries.DataComponentTypes COMPONENTS = UnifiedRegistries.DataComponentTypes.create(Unified.MOD_ID);

    public static final Supplied<DataComponentType<Integer>> FURNACE_FUEL = COMPONENTS.register(
            "furnace_fuel", builder -> builder.persistent(ExtraCodecs.POSITIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT)
    );

    public static final Supplied<DataComponentType<Float>> COMPOST = COMPONENTS.register(
            "compost", builder -> builder.persistent(ExtraCodecs.POSITIVE_FLOAT).networkSynchronized(ByteBufCodecs.FLOAT)
    );
}
