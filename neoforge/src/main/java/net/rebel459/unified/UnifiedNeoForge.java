package net.rebel459.unified;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.rebel459.unified.platform.NeoForgeUnifiedEvents;
import net.rebel459.unified.platform.NeoForgeHelpersImpl;
import net.rebel459.unified.platform.NeoForgeUnifiedRegistries;

@Mod(Unified.MOD_ID)
public class UnifiedNeoForge {

    public UnifiedNeoForge(IEventBus modEventBus) {
        NeoForgeUnifiedEvents.init(modEventBus);
        NeoForgeUnifiedRegistries.init();
        NeoForgeHelpersImpl.init();
        NeoForgeUnifiedRegistries.registerBus(Unified.MOD_ID, modEventBus);
        Unified.initRegistries();
        modEventBus.addListener(UnifiedNeoForge::commonSetup);
        modEventBus.addListener(NeoForgeHelpersImpl.StrippableBlocks::strippables);
        modEventBus.addListener(NeoForgeHelpersImpl.CreativeEntries::buildContents);
        modEventBus.addListener(NeoForgeHelpersImpl.Packs::addFeaturePacks);
        modEventBus.addListener(NeoForgeUnifiedRegistries.Blocks::modifyBlockEntities);
        modEventBus.addListener(NeoForgeHelpersImpl.NetworkPayloads::register);
        modEventBus.addListener(NeoForgeHelpersImpl.NetworkPayloads::registerWithHandler);
    }

    private static void commonSetup(final FMLCommonSetupEvent event) {
        Unified.init();
    }
}