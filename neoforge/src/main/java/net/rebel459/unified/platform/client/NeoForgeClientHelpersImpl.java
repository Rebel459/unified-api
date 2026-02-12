package net.rebel459.unified.platform.client;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.rebel459.unified.platform.Factory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class NeoForgeClientHelpersImpl {

    public static void init() {
        Factory.setClientHelpers(new Factory.ClientHelpers() {

            @Override
            public ClientHelpersImpl.ParticleProviders createParticleProviders() {
                return new ParticleProviders();
            }

            @Override
            public ClientHelpersImpl.EntityRenderers createEntityRenderers() {
                return new EntityRenderers();
            }

            @Override
            public ClientHelpersImpl.BlockLayers createBlockLayers() {
                return new BlockLayers();
            }

            @Override
            public ClientHelpersImpl.NetworkPayloads createNetworkPayloads() {
                return new NetworkPayloads();
            }
        });
    }

    public static class ParticleProviders implements ClientHelpersImpl.ParticleProviders {

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

    public static class EntityRenderers implements ClientHelpersImpl.EntityRenderers {

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

    public static class BlockLayers implements ClientHelpersImpl.BlockLayers {

        @Override
        public void add(Block block, ChunkSectionLayer layer) {
            ItemBlockRenderTypes.setRenderLayer(block, layer);
        }

        @Override
        public void add(Fluid fluid, ChunkSectionLayer layer) {
            ItemBlockRenderTypes.setRenderLayer(fluid, layer);
        }
    }

    public static class NetworkPayloads implements ClientHelpersImpl.NetworkPayloads {

        @Override
        public void send(CustomPacketPayload payload) {
            Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(payload));
        }
    }
}