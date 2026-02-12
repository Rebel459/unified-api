package net.rebel459.unified.registry;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.rebel459.unified.Unified;
import net.rebel459.unified.platform.UnifiedRegistries;

import java.util.function.Supplier;

public class UnifiedComponents {

    public static void init() {}

    public static UnifiedRegistries.ItemComponents COMPONENTS = UnifiedRegistries.ItemComponents.create(Unified.MOD_ID);

    public static final Supplier<DataComponentType<Integer>> FURNACE_FUEL = COMPONENTS.register(
            "furnace_fuel", builder -> builder.persistent(ExtraCodecs.POSITIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT)
    );
}
