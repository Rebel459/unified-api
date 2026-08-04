package net.rebel459.unified;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.rebel459.unified.neoforge.client.core.NeoForgeUnifiedClientEvents;
import net.rebel459.unified.neoforge.client.core.NeoForgeClientHelpers;
import net.rebel459.unified.neoforge.client.core.NeoForgeUnifiedClientRegistries;

@Mod(value = Unified.MOD_ID, dist = Dist.CLIENT)
public class UnifiedNeoForgeClient {

    public UnifiedNeoForgeClient(IEventBus modEventBus) {
        NeoForgeUnifiedClientEvents.init();
        UnifiedClient.init();
        modEventBus.addListener(NeoForgeClientHelpers.ParticleProviders::registerParticleProviders);
        modEventBus.addListener(NeoForgeClientHelpers.EntityRenderers::registerLayerDefinitions);
        modEventBus.addListener(NeoForgeClientHelpers.EntityRenderers::registerRenderers);
        modEventBus.addListener(NeoForgeUnifiedClientRegistries.KeyMappings::registerBindings);
        modEventBus.addListener(NeoForgeClientHelpers.Tooltips::registerTooltipFactories);
        modEventBus.addListener(NeoForgeClientHelpers.ResourcePacks::addFeaturePacks);
    }
}