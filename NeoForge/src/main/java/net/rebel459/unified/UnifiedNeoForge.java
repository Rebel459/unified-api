package net.rebel459.unified;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.rebel459.unified.platform.NeoForgeUnifiedRegistries;
import net.rebel459.unified.platform.RegistryFactory;
import net.rebel459.unified.platform.UnifiedRegistries;

@Mod(Unified.MOD_ID)
public class UnifiedNeoForge {

    public UnifiedNeoForge(IEventBus modEventBus) {
        NeoForgeUnifiedRegistries.init();
        NeoForgeUnifiedRegistries.registerBus(Unified.MOD_ID, modEventBus);
        Unified.initialize();
    }
}