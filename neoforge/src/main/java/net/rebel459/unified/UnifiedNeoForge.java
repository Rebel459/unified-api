package net.rebel459.unified;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.rebel459.unified.neoforge.NeoForgeUnifiedBus;
import net.rebel459.unified.neoforge.core.NeoForgeHelpers;
import net.rebel459.unified.neoforge.core.NeoForgeUnifiedEvents;
import net.rebel459.unified.neoforge.core.NeoForgeUnifiedRegistries;

@Mod(Unified.MOD_ID)
public class UnifiedNeoForge {

    public UnifiedNeoForge(IEventBus modEventBus) {
        NeoForgeUnifiedEvents.init(modEventBus);
        NeoForgeUnifiedBus.register(Unified.MOD_ID, modEventBus);
        Unified.initRegistries();
        modEventBus.addListener(UnifiedNeoForge::commonSetup);
        modEventBus.addListener(NeoForgeHelpers.CreativeEntries::buildContents);
        modEventBus.addListener(NeoForgeHelpers.DataPacks::addFeaturePacks);
        modEventBus.addListener(NeoForgeUnifiedRegistries.Blocks::modifyBlockEntities);
        modEventBus.addListener(NeoForgeHelpers.Networking::register);
        modEventBus.addListener(NeoForgeHelpers.Networking::registerWithHandler);
        modEventBus.addListener(NeoForgeUnifiedRegistries.EntityTypes::createEntityAttributes);
        NeoForge.EVENT_BUS.addListener(NeoForgeHelpers.ReloadListeners::addServerReloadListeners);
        modEventBus.addListener(NeoForgeHelpers.DataRegistries::registerDataRegistries);
        modEventBus.addListener(NeoForgeHelpers.SpawnPlacements::registerSpawnPlacements);
    }

    private static void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(Unified::init);
    }
}
