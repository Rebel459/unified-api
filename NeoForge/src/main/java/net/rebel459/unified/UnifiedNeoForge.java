package net.rebel459.unified;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.rebel459.unified.platform.NeoForgeUnifiedRegistries;
import net.rebel459.unified.platform.RegistryFactory;
import net.rebel459.unified.platform.UnifiedRegistries;
import net.rebel459.unified.test.UnifiedTest;

@Mod(Unified.MOD_ID)
public class UnifiedNeoForge {

    public UnifiedNeoForge(IEventBus modEventBus) {
        NeoForgeUnifiedRegistries.init();
        NeoForgeUnifiedRegistries.registerBus(Unified.MOD_ID, modEventBus);
        Unified.initRegistries();
        modEventBus.addListener(UnifiedNeoForge::commonSetup);
        //modEventBus.addListener(NeoForgeUnifiedRegistries.FuelRegistry::event);
        modEventBus.addListener(NeoForgeUnifiedRegistries.CreativeRegistry::buildContents);
    }

    private static void commonSetup(final FMLCommonSetupEvent event) {
        Unified.init();
    }
}