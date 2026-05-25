package net.rebel459.unified.platform.client;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Function;
import java.util.function.Supplier;

public class ClientHelpersImpl {

    public interface ParticleProviders {

        <T extends ParticleOptions> void add(Supplier<T> type, ParticleResources.SpriteParticleRegistration<T> sprite);

        static ParticleProviders get() {
            return ClientInternalHandlerImpl.INSTANCE.getParticleProviders();
        }
    }

    public interface EntityRenderers {

        void addLayerDefinition(ModelLayerLocation location, Supplier<LayerDefinition> definition);

        <T extends Entity> void addEntityRenderer(Supplier<EntityType<? extends T>> entityType, EntityRendererProvider<T> entityRendererProvider);

        <T extends BlockEntity, S extends BlockEntityRenderState> void addBlockEntityRenderer(Supplier<BlockEntityType<? extends T>> blockEntityType, BlockEntityRendererProvider<T, S> blockEntityRendererProvider);

        static EntityRenderers get() {
            return ClientInternalHandlerImpl.INSTANCE.getEntityRenderers();
        }
    }

    public interface Networking {

        boolean canSend(CustomPacketPayload payload);

        void send(CustomPacketPayload payload);

        static Networking get() {
            return ClientInternalHandlerImpl.INSTANCE.getNetworking();
        }
    }

    public interface Tooltips {

        <T extends TooltipComponent> void bind(Class<T> type, Function<T, ClientTooltipComponent> factory);

        static Tooltips get() {
            return ClientInternalHandlerImpl.INSTANCE.getTooltips();
        }
    }
}
