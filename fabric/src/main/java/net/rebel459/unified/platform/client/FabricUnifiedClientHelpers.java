package net.rebel459.unified.platform.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.rebel459.unified.platform.Factory;
import net.rebel459.unified.util.MutableSpriteSet;

import java.util.function.Supplier;

public class FabricUnifiedClientHelpers {

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

        @Override
        public void add(Supplier<ParticleType> type, ParticleResources.SpriteParticleRegistration sprite) {
            ParticleFactoryRegistry.getInstance().register(type.get(), sprite.create(new MutableSpriteSet()));
        }
    }

    public static class EntityRenderers implements ClientHelpersImpl.EntityRenderers {

        @Override
        public void addLayerDefinition(ModelLayerLocation location, Supplier<LayerDefinition> definition) {
            EntityModelLayerRegistry.registerModelLayer(location, (EntityModelLayerRegistry.TexturedModelDataProvider) definition);
        }

        @Override
        public <T extends Entity> void addEntityRenderer(EntityType<? extends T> entityType, EntityRendererProvider<T> entityRendererProvider) {
            EntityRendererRegistry.register(entityType, entityRendererProvider);
        }

        @Override
        public <T extends BlockEntity, S extends BlockEntityRenderState> void addBlockEntityRenderer(BlockEntityType<? extends T> blockEntityType, BlockEntityRendererProvider<T, S> blockEntityRendererProvider) {
            BlockEntityRendererRegistry.register(blockEntityType, blockEntityRendererProvider);
        }
    }

    public static class BlockLayers implements ClientHelpersImpl.BlockLayers {

        @Override
        public void add(Block block, ChunkSectionLayer layer) {
            BlockRenderLayerMap.putBlock(block, layer);
        }

        @Override
        public void add(Fluid fluid, ChunkSectionLayer layer) {
            BlockRenderLayerMap.putFluid(fluid, layer);
        }
    }

    public static class NetworkPayloads implements ClientHelpersImpl.NetworkPayloads {

        @Override
        public void send(CustomPacketPayload payload) {
            ClientPlayNetworking.send(payload);
        }
    }
}