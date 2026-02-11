package net.rebel459.unified;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.rebel459.unified.platform.NeoForgeUnifiedEvents;
import net.rebel459.unified.platform.NeoForgeUnifiedRegistries;
import net.rebel459.unified.test.UnifiedTest;

import java.util.HashMap;

@Mod(Unified.MOD_ID)
public class UnifiedNeoForge {

    public UnifiedNeoForge(IEventBus modEventBus) {
        NeoForgeUnifiedRegistries.init();
        NeoForgeUnifiedEvents.init();
        NeoForgeUnifiedRegistries.registerBus(Unified.MOD_ID, modEventBus);
        Unified.initRegistries();
        modEventBus.addListener(UnifiedNeoForge::commonSetup);
        modEventBus.addListener(NeoForgeUnifiedEvents.StrippableEvent::strippables);
        modEventBus.addListener(NeoForgeUnifiedEvents.CreativeEvent::buildContents);
        modEventBus.addListener(NeoForgeUnifiedEvents.PackEvent::addFeaturePacks);
        modEventBus.addListener(NeoForgeUnifiedRegistries.BlockRegistry::modifyBlockEntities);
    }

    private static void commonSetup(final FMLCommonSetupEvent event) {
        Unified.init();
    }
}