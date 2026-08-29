package net.rebel459.unified.neoforge;

import net.minecraft.core.Registry;
import net.neoforged.bus.api.IEventBus;
import net.rebel459.unified.neoforge.core.NeoForgeUnifiedRegistries;

import java.util.Arrays;


/** Register your mod's IEventBus here prior to calling Mod.initRegistries() from NeoForge-side */
public final class NeoForgeUnifiedBus {

    @SafeVarargs
    public static <T extends Registry<?>> void register(String modId, IEventBus modEventBus, T... registries) {
        NeoForgeUnifiedRegistries.registerBus(modId, modEventBus, Arrays.stream(registries).toList());
    }
}
