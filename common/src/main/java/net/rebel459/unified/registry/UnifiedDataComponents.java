package net.rebel459.unified.registry;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.rebel459.unified.Unified;
import net.rebel459.unified.platform.UnifiedRegistries;
import net.rebel459.unified.util.Supplied;

import java.util.function.Supplier;

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
