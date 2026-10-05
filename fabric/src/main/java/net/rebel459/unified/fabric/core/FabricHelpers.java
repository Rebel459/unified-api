package net.rebel459.unified.fabric.core;

import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.levelgen.Heightmap;
import net.rebel459.unified.impl.core.CommonHelpers;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FabricHelpers {

    public static class DataPacks implements CommonHelpers.DataPacks {

        @Override
        public void addRequired(Identifier id) {
            add(id, true);
        }

        @Override
        public void addOptional(Identifier id) {
            add(id, false);
        }

        private static void add(Identifier id, boolean required) {
            PackActivationType type = PackActivationType.NORMAL;
            if (required) type = PackActivationType.ALWAYS_ENABLED;
            ResourceLoader.registerBuiltinPack(
                    id, FabricLoader.getInstance().getModContainer(id.getNamespace()).get(),
                    Component.translatable("pack." + id.getNamespace() + "." + id.getPath()),
                    type
            );
        }
    }

    public static class Networking implements CommonHelpers.Networking {

        @Override
        public void registerPlayToServer(CustomPacketPayload.Type type, StreamCodec codec) {
            PayloadTypeRegistry.serverboundPlay().register(type, codec);
        }

        @Override
        public void registerPlayToClient(CustomPacketPayload.Type type, StreamCodec codec) {
            PayloadTypeRegistry.clientboundPlay().register(type, codec);
        }

        @Override
        public void registerPlayToServer(CustomPacketPayload.Type type, StreamCodec codec, BiConsumer handler) {

            PayloadTypeRegistry.serverboundPlay().register(type, codec);

            ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
                handler.accept(payload, context.player());
            });
        }

        @Override
        public void registerPlayToClient(CustomPacketPayload.Type type, StreamCodec codec, BiConsumer handler) {

            PayloadTypeRegistry.clientboundPlay().register(type, codec);

            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
                    handler.accept(payload, context.player());
                });
            }
        }

        @Override
        public void registerConfigToServer(CustomPacketPayload.Type type, StreamCodec codec) {
            PayloadTypeRegistry.serverboundConfiguration().register(type, codec);
        }

        @Override
        public void registerConfigToClient(CustomPacketPayload.Type type, StreamCodec codec) {
            PayloadTypeRegistry.clientboundConfiguration().register(type, codec);
        }

        @Override
        public void registerConfigToServer(CustomPacketPayload.Type type, StreamCodec codec, Consumer handler) {

            PayloadTypeRegistry.serverboundConfiguration().register(type, codec);

            ServerConfigurationNetworking.registerGlobalReceiver(type, (payload, context) ->
                    context.server().execute(() -> handler.accept(payload)));
        }

        @Override
        public void registerConfigToClient(CustomPacketPayload.Type type, StreamCodec codec, Consumer handler) {

            PayloadTypeRegistry.clientboundConfiguration().register(type, codec);

            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                ClientConfigurationNetworking.registerGlobalReceiver(type, (payload, context) ->
                        context.client().execute(() -> handler.accept(payload)));
            }
        }

        @Override
        public boolean canSend(CustomPacketPayload payload, ServerPlayer player) {
            return ServerPlayNetworking.canSend(player, payload.type());
        }

        @Override
        public void send(CustomPacketPayload payload, ServerPlayer player) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static class ReloadListeners implements CommonHelpers.ReloadListeners {

        @Override
        public void addListener(Identifier id, PreparableReloadListener listener) {
            ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(id, listener);
        }

        @Override
        public void addOrdering(Identifier first, Identifier second) {
            ResourceLoader.get(PackType.SERVER_DATA).addListenerOrdering(first, second);
        }
    }

    public static class DataRegistries implements CommonHelpers.DataRegistries {

        @Override
        public <T> void register(ResourceKey<Registry<T>> key, Codec<T> codec) {
            DynamicRegistries.register(key, codec);
        }

        @Override
        public <T> void registerSynced(ResourceKey<Registry<T>> key, Codec<T> serverCodec, Codec<T> clientCodec) {
            DynamicRegistries.registerSynced(key, serverCodec, clientCodec);
        }
    }

    public static class EntityData implements CommonHelpers.EntityData {

        @Override
        public void registerSerializer(Identifier id, Supplier<EntityDataSerializer<?>> serializer) {
            FabricEntityDataRegistry.register(id, serializer.get());
        }
    }

    public static class SpawnPlacements implements CommonHelpers.SpawnPlacements {

        @Override
        public <T extends Mob> void register(Supplier<EntityType<T>> type, SpawnPlacementType placementType, Heightmap.Types heightmap, net.minecraft.world.entity.SpawnPlacements.SpawnPredicate<T> spawnPredicate) {
            net.minecraft.world.entity.SpawnPlacements.register(type.get(), placementType, heightmap, spawnPredicate);
        }
    }
}
