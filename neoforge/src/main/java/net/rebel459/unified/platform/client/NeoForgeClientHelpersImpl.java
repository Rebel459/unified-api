package net.rebel459.unified.platform.client;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class NeoForgeClientHelpersImpl {

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
        public static List<Pair<Supplier, EntityRendererProvider>> ENTITY_RENDERERS = new ArrayList<>();
        public static List<Pair<Supplier, BlockEntityRendererProvider>> BLOCK_ENTITY_RENDERERS = new ArrayList<>();

        @Override
        public void addLayerDefinition(ModelLayerLocation location, Supplier<LayerDefinition> definition) {
            LAYER_DEFINITIONS.add(Pair.of(location, definition));
        }

        @Override
        public <T extends Entity> void addEntityRenderer(Supplier<EntityType<? extends T>> entityType, EntityRendererProvider<T> entityRendererProvider) {
            ENTITY_RENDERERS.add(Pair.of(entityType, entityRendererProvider));
        }

        @Override
        public <T extends BlockEntity, S extends BlockEntityRenderState> void addBlockEntityRenderer(Supplier<BlockEntityType<? extends T>> blockEntityType, BlockEntityRendererProvider<T, S> blockEntityRendererProvider) {
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
            for (Pair<Supplier, EntityRendererProvider> entityRenderers : ENTITY_RENDERERS) {
                event.registerEntityRenderer((EntityType) entityRenderers.getFirst().get(), entityRenderers.getSecond());
            }
            for (Pair<Supplier, BlockEntityRendererProvider> blockEntityRenderers : BLOCK_ENTITY_RENDERERS) {
                event.registerBlockEntityRenderer((BlockEntityType) blockEntityRenderers.getFirst().get(), blockEntityRenderers.getSecond());
            }
        }
    }

    public static class Networking implements ClientHelpersImpl.Networking {

        @Override
        public void send(CustomPacketPayload payload) {
            var connection = Minecraft.getInstance().getConnection();
            if (connection != null) connection.send(new ServerboundCustomPayloadPacket(payload));
        }
    }


    public static class Tooltips implements ClientHelpersImpl.Tooltips {

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