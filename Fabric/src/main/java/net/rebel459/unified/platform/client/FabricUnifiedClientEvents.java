package net.rebel459.unified.platform.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.rebel459.unified.platform.UnifiedEvents;
import net.rebel459.unified.platform.UnifiedFactory;

import java.util.function.Supplier;

public class FabricUnifiedClientEvents {

    public static void init() {
        UnifiedFactory.setClientEvents(new UnifiedFactory.ClientEvents() {

            @Override
            public UnifiedClientEvents.ParticleProviders createParticleProviders() {
                return new ParticleProviders();
            }

            @Override
            public UnifiedClientEvents.EntityRenderers createEntityRenderers() {
                return new EntityRenderers();
            }

            @Override
            public UnifiedClientEvents.BlockLayers createBlockLayers() {
                return new BlockLayers();
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            UnifiedEvents.ClientTickEvent.pass(client);
        });
    }

    public static class ParticleProviders implements UnifiedClientEvents.ParticleProviders {

        @Override
        public <T extends ParticleOptions> void add(ParticleType<T> type, ParticleResources.SpriteParticleRegistration<T> provider) {
            ParticleFactoryRegistry.getInstance().register(type, (ParticleFactoryRegistry.PendingParticleFactory) provider);
        }
    }

    public static class EntityRenderers implements UnifiedClientEvents.EntityRenderers {

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

    public static class BlockLayers implements UnifiedClientEvents.BlockLayers {

        @Override
        public void add(Block block, ChunkSectionLayer layer) {
            BlockRenderLayerMap.putBlock(block, layer);
        }

        @Override
        public void add(Fluid fluid, ChunkSectionLayer layer) {
            BlockRenderLayerMap.putFluid(fluid, layer);
        }
    }
}