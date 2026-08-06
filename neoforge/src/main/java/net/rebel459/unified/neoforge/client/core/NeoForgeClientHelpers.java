package net.rebel459.unified.neoforge.client.core;

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
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.rebel459.unified.impl.client.builder.WoodSetClientProperties;
import net.rebel459.unified.impl.client.core.CommonClientHelpers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class NeoForgeClientHelpers {

    public static class ParticleProviders implements CommonClientHelpers.ParticleProviders {

        private static final List<ParticleProviderRegistration> PARTICLE_PROVIDERS = new ArrayList<>();

        @Override
        public <T extends ParticleOptions> void add(Supplier<? extends ParticleType<T>> type, ParticleResources.SpriteParticleRegistration<T> sprite) {
            PARTICLE_PROVIDERS.add(event -> event.registerSpriteSet(type.get(), sprite));
        }

        @SubscribeEvent
        public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
            for (ParticleProviderRegistration registration : PARTICLE_PROVIDERS) {
                registration.register(event);
            }
        }

        @FunctionalInterface
        private interface ParticleProviderRegistration {
            void register(RegisterParticleProvidersEvent event);
        }
    }

    public static class EntityRenderers implements CommonClientHelpers.EntityRenderers {

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
            WoodSetClientProperties.init(true, false);
            for (Pair<ModelLayerLocation, Supplier<LayerDefinition>> layerDefinitions : LAYER_DEFINITIONS) {
                event.registerLayerDefinition(layerDefinitions.getFirst(), layerDefinitions.getSecond());
            }
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            WoodSetClientProperties.init(false, true);
            for (Pair<Supplier, EntityRendererProvider> entityRenderers : ENTITY_RENDERERS) {
                event.registerEntityRenderer((EntityType) entityRenderers.getFirst().get(), entityRenderers.getSecond());
            }
            for (Pair<Supplier, BlockEntityRendererProvider> blockEntityRenderers : BLOCK_ENTITY_RENDERERS) {
                event.registerBlockEntityRenderer((BlockEntityType) blockEntityRenderers.getFirst().get(), blockEntityRenderers.getSecond());
            }
        }
    }

    public static class Networking implements CommonClientHelpers.Networking {

        @Override
        public boolean canSend(CustomPacketPayload payload) {
            var connection = Minecraft.getInstance().getConnection();
            return (connection != null && connection.hasChannel(payload.type()));
        }

        @Override
        public void send(CustomPacketPayload payload) {
            var connection = Minecraft.getInstance().getConnection();
            if (connection != null && connection.hasChannel(payload.type())) connection.send(new ServerboundCustomPayloadPacket(payload));
        }
    }


    public static class Tooltips implements CommonClientHelpers.Tooltips {

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

    public static class ResourcePacks implements CommonClientHelpers.ResourcePacks {

        public static List<Pair<Identifier, Boolean>> PACK_LIST = new ArrayList<>();

        @Override
        public void addRequired(Identifier id) {
            PACK_LIST.add(Pair.of(id, true));
        }

        @Override
        public void addOptional(Identifier id) {
            PACK_LIST.add(Pair.of(id, false));
        }

        @SubscribeEvent
        public static void addFeaturePacks(AddPackFindersEvent event) {
            for (Pair<Identifier, Boolean> pair : PACK_LIST) {
                Identifier id = pair.getFirst();
                boolean required = pair.getSecond();

                event.addPackFinders(
                        Identifier.fromNamespaceAndPath(id.getNamespace(), "resourcepacks/" + id.getPath()),
                        PackType.CLIENT_RESOURCES,
                        Component.translatable("pack." + id.getNamespace() + "." + id.getPath()),
                        PackSource.BUILT_IN,
                        required,
                        Pack.Position.TOP
                );
            }
        }
    }

    public static class ReloadListeners implements CommonClientHelpers.ReloadListeners {

        private static List<Pair<Identifier, PreparableReloadListener>> LISTENERS = new ArrayList<>();
        private static List<Pair<Identifier, Identifier>> ORDERING = new ArrayList<>();

        @Override
        public void addListener(Identifier id, PreparableReloadListener listener) {
            LISTENERS.add(Pair.of(id, listener));
        }

        @Override
        public void addOrdering(Identifier first, Identifier second) {
            ORDERING.add(Pair.of(first, second));
        }

        @SubscribeEvent
        public static void addClientReloadListeners(final AddClientReloadListenersEvent event) {
            LISTENERS.forEach(pair -> event.addListener(pair.getFirst(), pair.getSecond()));
            ORDERING.forEach(pair -> event.addDependency(pair.getFirst(), pair.getSecond()));
        }
    }
}