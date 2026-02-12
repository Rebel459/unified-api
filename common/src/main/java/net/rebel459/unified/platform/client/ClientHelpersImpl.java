package net.rebel459.unified.platform.client;

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

import java.util.function.Supplier;

public class ClientHelpersImpl {

    public interface ParticleProviders {

        <T extends ParticleOptions> void add(Supplier<T> type, ParticleResources.SpriteParticleRegistration<T> sprite);
    }

    public interface EntityRenderers {

        void addLayerDefinition(ModelLayerLocation location, Supplier<LayerDefinition> definition);

        <T extends Entity> void addEntityRenderer(EntityType<? extends T> entityType, EntityRendererProvider<T> entityRendererProvider);

        <T extends BlockEntity, S extends BlockEntityRenderState> void addBlockEntityRenderer(BlockEntityType<? extends T> blockEntityType, BlockEntityRendererProvider<T, S> blockEntityRendererProvider);
    }

    public interface BlockLayers {

        void add(Block block, ChunkSectionLayer layer);
        void add(Fluid fluid, ChunkSectionLayer layer);
    }

    public interface NetworkPayloads {

        void send(CustomPacketPayload payload);
    }
}
