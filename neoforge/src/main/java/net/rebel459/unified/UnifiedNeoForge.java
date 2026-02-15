package net.rebel459.unified;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.rebel459.unified.platform.NeoForgeUnifiedEvents;
import net.rebel459.unified.platform.NeoForgeUnifiedHelpers;
import net.rebel459.unified.platform.NeoForgeUnifiedRegistries;

@Mod(Unified.MOD_ID)
public class UnifiedNeoForge {

    public UnifiedNeoForge(IEventBus modEventBus) {
        NeoForgeUnifiedEvents.init(modEventBus);
        NeoForgeUnifiedRegistries.registerBus(Unified.MOD_ID, modEventBus);
        Unified.initRegistries();
        modEventBus.addListener(UnifiedNeoForge::commonSetup);
        modEventBus.addListener(NeoForgeUnifiedHelpers.StrippableBlocks::strippables);
        modEventBus.addListener(NeoForgeUnifiedHelpers.CreativeEntries::buildContents);
        modEventBus.addListener(NeoForgeUnifiedHelpers.Packs::addFeaturePacks);
        modEventBus.addListener(NeoForgeUnifiedRegistries.Blocks::modifyBlockEntities);
        modEventBus.addListener(NeoForgeUnifiedHelpers.NetworkPayloads::register);
        modEventBus.addListener(NeoForgeUnifiedHelpers.NetworkPayloads::registerWithHandler);
    }

    private static void commonSetup(final FMLCommonSetupEvent event) {
        Unified.init();
    }
}