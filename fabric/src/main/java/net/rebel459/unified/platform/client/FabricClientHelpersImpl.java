package net.rebel459.unified.platform.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
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
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Function;
import java.util.function.Supplier;

public class FabricClientHelpersImpl {

    public static class ParticleProviders implements ClientHelpersImpl.ParticleProviders {

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        public <T extends ParticleOptions> void add(Supplier<T> type, ParticleResources.SpriteParticleRegistration<T> sprite) {
            ParticleFactoryRegistry.getInstance().register((ParticleType) type.get(), sprite::create);
        }
    }

    public static class EntityRenderers implements ClientHelpersImpl.EntityRenderers {

        @Override
        public void addLayerDefinition(ModelLayerLocation location, Supplier<LayerDefinition> definition) {
            EntityModelLayerRegistry.registerModelLayer(location, definition::get);
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

    public static class Networking implements ClientHelpersImpl.Networking {

        @Override
        public void send(CustomPacketPayload payload) {
            ClientPlayNetworking.send(payload);
        }
    }

    public static class Tooltips implements ClientHelpersImpl.Tooltips {

        @Override
        public <T extends TooltipComponent> void bind(Class<T> type, Function<T, ClientTooltipComponent> factory) {
            TooltipComponentCallback.EVENT.register(component -> {
                if (type.isInstance(component)) {
                    return factory.apply(type.cast(component));
                }
                return null;
            });
        }
    }
}