package net.rebel459.unified;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.rebel459.unified.platform.NeoForgeUnifiedRegistries;

@Mod(Unified.MOD_ID)
public class UnifiedNeoForge {

    public UnifiedNeoForge(IEventBus modEventBus) {
        NeoForgeUnifiedRegistries.init();                // sets factory, ensures registers exist early
        NeoForgeUnifiedRegistries.registerBus("unified", modEventBus);  // attaches them to bus
        Unified.initialize();
    }
}