package net.rebel459.unified;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.rebel459.unified.platform.NeoForgeUnifiedEvents;
import net.rebel459.unified.platform.NeoForgeUnifiedRegistries;

@Mod(Unified.MOD_ID)
public class UnifiedNeoForge {

    public UnifiedNeoForge(IEventBus modEventBus) {
        NeoForgeUnifiedRegistries.init();
        NeoForgeUnifiedEvents.init();
        NeoForgeUnifiedRegistries.registerBus(Unified.MOD_ID, modEventBus);
        Unified.initRegistries();
        modEventBus.addListener(UnifiedNeoForge::commonSetup);
        modEventBus.addListener(NeoForgeUnifiedEvents.StrippableBlocks::strippables);
        modEventBus.addListener(NeoForgeUnifiedEvents.CreativeEntries::buildContents);
        modEventBus.addListener(NeoForgeUnifiedEvents.Packs::addFeaturePacks);
        modEventBus.addListener(NeoForgeUnifiedRegistries.Blocks::modifyBlockEntities);
    }

    private static void commonSetup(final FMLCommonSetupEvent event) {
        Unified.init();
    }
}