package net.rebel459.unified.platform.client;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.rebel459.unified.platform.UnifiedHelpers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class NeoForgeUnifiedClientHelpers {

    public static class ParticleProviders implements UnifiedClientHelpers.ParticleProviders {

        public static List<Pair<Supplier, ParticleResources.SpriteParticleRegistration>> PARTICLE_PROVIDERS = new ArrayList<>();

        @Override
        public <T extends ParticleOptions> void add(Supplier<T> type, ParticleResources.SpriteParticleRegistration<T> sprite) {
            PARTICLE_PROVIDERS.add(Pair.of(type, sprite));
        }

        @SubscribeEvent
        public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
            for (Pair<Supplier, ParticleResources.SpriteParticleRegistration> pair : PARTICLE_PROVIDERS) {
                event.registerSpriteSet((ParticleType<? extends ParticleOptions>) pair.getFirst().get(), pair.getSecond());
            }
        }
    }

    public static class EntityRenderers implements UnifiedClientHelpers.EntityRenderers {

        public static List<Pair<ModelLayerLocation, Supplier<LayerDefinition>>> LAYER_DEFINITIONS = new ArrayList<>();
        public static List<Pair<EntityType, EntityRendererProvider>> ENTITY_RENDERERS = new ArrayList<>();
        public static List<Pair<BlockEntityType, BlockEntityRendererProvider>> BLOCK_ENTITY_RENDERERS = new ArrayList<>();

        @Override
        public void addLayerDefinition(ModelLayerLocation location, Supplier<LayerDefinition> definition) {
            LAYER_DEFINITIONS.add(Pair.of(location, definition));
        }

        @Override
        public <T extends Entity> void addEntityRenderer(EntityType<? extends T> entityType, EntityRendererProvider<T> entityRendererProvider) {
            ENTITY_RENDERERS.add(Pair.of(entityType, entityRendererProvider));
        }

        @Override
        public <T extends BlockEntity, S extends BlockEntityRenderState> void addBlockEntityRenderer(BlockEntityType<? extends T> blockEntityType, BlockEntityRendererProvider<T, S> blockEntityRendererProvider) {
            BLOCK_ENTITY_RENDERERS.add(Pair.of(blockEntityType, blockEntityRendererProvider));
        }

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            for (Pair<ModelLayerLocation, Supplier<LayerDefinition>> layerDefinitions : LAYER_DEFINITIONS) {
                event.registerLayerDefinition(layerDefinitions.getFirst(), layerDefinitions.getSecond());
            }
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            for (Pair<EntityType, EntityRendererProvider> entityRenderers : ENTITY_RENDERERS) {
                event.registerEntityRenderer(entityRenderers.getFirst(), entityRenderers.getSecond());
            }
            for (Pair<BlockEntityType, BlockEntityRendererProvider> blockEntityRenderers : BLOCK_ENTITY_RENDERERS) {
                event.registerBlockEntityRenderer(blockEntityRenderers.getFirst(), blockEntityRenderers.getSecond());
            }
        }
    }

    public static class BlockLayers implements UnifiedClientHelpers.BlockLayers {

        @Override
        public void add(Block block, ChunkSectionLayer layer) {
            ItemBlockRenderTypes.setRenderLayer(block, layer);
        }

        @Override
        public void add(Fluid fluid, ChunkSectionLayer layer) {
            ItemBlockRenderTypes.setRenderLayer(fluid, layer);
        }
    }

    public static class NetworkPayloads implements UnifiedClientHelpers.NetworkPayloads {

        @Override
        public void send(CustomPacketPayload payload) {
            Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(payload));
        }
    }


    public static class Tooltips implements UnifiedClientHelpers.Tooltips {

        private record Bindings<T extends TooltipComponent>(Class<T> type, Function<T, ClientTooltipComponent> factory) {}

        private static final List<Bindings<?>> TOOLTIPS = new ArrayList<>();

        @Override
        public <T extends TooltipComponent> void bind(Class<T> type, Function<T, ClientTooltipComponent> factory) {
            TOOLTIPS.add(new Bindings<>(type, factory));
        }

        @SubscribeEvent
        public static void registerTooltipFactories(RegisterClientTooltipComponentFactoriesEvent event) {
            for (Bindings<?> list : TOOLTIPS) {
                @SuppressWarnings("unchecked")
                Bindings<TooltipComponent> bindings = (Bindings<TooltipComponent>) list;
                event.register(bindings.type, bindings.factory);
            }
            TOOLTIPS.clear();
        }
    }
}