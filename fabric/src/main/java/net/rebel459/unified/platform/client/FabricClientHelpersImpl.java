package net.rebel459.unified.platform.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Function;
import java.util.function.Supplier;

public class FabricClientHelpersImpl {

    public static class ParticleProviders implements ClientHelpersImpl.ParticleProviders {

        @Override
        public <T extends ParticleOptions> void add(Supplier<? extends ParticleType<T>> type, ParticleResources.SpriteParticleRegistration<T> sprite) {
            ParticleProviderRegistry.getInstance().register(type.get(), sprite::create);
        }
    }

    public static class EntityRenderers implements ClientHelpersImpl.EntityRenderers {

        @Override
        public void addLayerDefinition(ModelLayerLocation location, Supplier<LayerDefinition> definition) {
            ModelLayerRegistry.registerModelLayer(location, definition::get);
        }

        @Override
        public <T extends Entity> void addEntityRenderer(Supplier<EntityType<? extends T>> entityType, EntityRendererProvider<T> entityRendererProvider) {
            net.minecraft.client.renderer.entity.EntityRenderers.register(entityType.get(), entityRendererProvider);
        }

        @Override
        public <T extends BlockEntity, S extends BlockEntityRenderState> void addBlockEntityRenderer(Supplier<BlockEntityType<? extends T>> blockEntityType, BlockEntityRendererProvider<T, S> blockEntityRendererProvider) {
            BlockEntityRenderers.register(blockEntityType.get(), blockEntityRendererProvider);
        }
    }

    public static class Networking implements ClientHelpersImpl.Networking {

        @Override
        public boolean canSend(CustomPacketPayload payload) {
            return ClientPlayNetworking.canSend(payload.type());
        }

        @Override
        public void send(CustomPacketPayload payload) {
            if (ClientPlayNetworking.canSend(payload.type())) {
                ClientPlayNetworking.send(payload);
            }
        }
    }

    public static class Tooltips implements ClientHelpersImpl.Tooltips {

        @Override
        public <T extends TooltipComponent> void bind(Class<T> type, Function<T, ClientTooltipComponent> factory) {
            ClientTooltipComponentCallback.EVENT.register(component -> {
                if (type.isInstance(component)) {
                    return factory.apply(type.cast(component));
                }
                return null;
            });
        }
    }

    public static class ReloadListeners implements ClientHelpersImpl.ReloadListeners {

        @Override
        public void addListener(Identifier id, PreparableReloadListener listener) {
            ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(id, listener);
        }

        @Override
        public void addOrdering(Identifier first, Identifier second) {
            ResourceLoader.get(PackType.CLIENT_RESOURCES).addListenerOrdering(first, second);
        }
    }
}