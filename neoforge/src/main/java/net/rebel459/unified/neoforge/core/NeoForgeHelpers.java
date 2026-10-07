package net.rebel459.unified.neoforge.core;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handlers.ServerPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.rebel459.unified.api.core.UnifiedRegistries;
import net.rebel459.unified.impl.core.CommonHelpers;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NeoForgeHelpers {

    public static class DataPacks implements CommonHelpers.DataPacks {

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
                        PackType.SERVER_DATA,
                        Component.translatable("pack." + id.getNamespace() + "." + id.getPath()),
                        PackSource.BUILT_IN,
                        required,
                        Pack.Position.TOP
                );
            }
        }
    }

    public static class Networking implements CommonHelpers.Networking {

        private static final List<ToServer> TO_SERVER_LIST = new ArrayList<>();
        private static final List<ToClient> TO_CLIENT_LIST = new ArrayList<>();

        private record ToServer<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type, StreamCodec<FriendlyByteBuf, T> codec, boolean play) {}
        private record ToClient<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type, StreamCodec<FriendlyByteBuf, T> codec, boolean play) {}

        @Override
        public void registerPlayToServer(CustomPacketPayload.Type type, StreamCodec codec) {
            TO_SERVER_LIST.add(new ToServer<CustomPacketPayload>(type, codec, true));
        }

        @Override
        public void registerPlayToClient(CustomPacketPayload.Type type, StreamCodec codec) {
            TO_CLIENT_LIST.add(new ToClient<CustomPacketPayload>(type, codec, true));
        }

        @Override
        public void registerConfigToServer(CustomPacketPayload.Type type, StreamCodec codec) {
            TO_SERVER_LIST.add(new ToServer<CustomPacketPayload>(type, codec, false));
        }

        @Override
        public void registerConfigToClient(CustomPacketPayload.Type type, StreamCodec codec) {
            TO_CLIENT_LIST.add(new ToClient<CustomPacketPayload>(type, codec, false));
        }

        @Override
        public boolean canSend(CustomPacketPayload payload, ServerPlayer player) {
            return player.connection.hasChannel(payload.type());
        }

        @Override
        public void send(CustomPacketPayload payload, ServerPlayer player) {
            player.connection.send(new ClientboundCustomPayloadPacket(payload));
        }

        @SubscribeEvent
        public static void register(RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar("1").optional();
            for (ToServer entry : TO_SERVER_LIST) {
                if (entry.play) {
                    registrar.playToServer(
                            entry.type,
                            entry.codec,
                            ServerPayloadHandler::handle
                    );
                } else {
                    registrar.configurationToServer(
                            entry.type,
                            entry.codec,
                            ServerPayloadHandler::handle
                    );
                }
            }
            for (ToClient entry : TO_CLIENT_LIST) {
                if (entry.play) {
                    registrar.playToClient(
                            entry.type,
                            entry.codec
                    );
                } else {
                    registrar.configurationToClient(
                            entry.type,
                            entry.codec
                    );
                }
            }
        }

        private static final List<HandledToServer<?>> HANDLED_TO_SERVER_LIST = new ArrayList<>();
        private static final List<HandledToClient<?>> HANDLED_TO_CLIENT_LIST = new ArrayList<>();
        private static final List<HandledConfig<?>> HANDLED_CONFIG_TO_SERVER_LIST = new ArrayList<>();
        private static final List<HandledConfig<?>> HANDLED_CONFIG_TO_CLIENT_LIST = new ArrayList<>();

        private record HandledToServer<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type, StreamCodec<FriendlyByteBuf, T> codec, BiConsumer<T, ServerPlayer> handler) {}
        private record HandledToClient<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type, StreamCodec<FriendlyByteBuf, T> codec, BiConsumer<T, Player> handler) {}
        private record HandledConfig<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type, StreamCodec<FriendlyByteBuf, T> codec, Consumer<T> handler) {}

        @Override
        public void registerPlayToServer(CustomPacketPayload.Type type, StreamCodec codec, BiConsumer handler) {
            HANDLED_TO_SERVER_LIST.add(new HandledToServer<>(type, codec, handler));
        }

        @Override
        public void registerPlayToClient(CustomPacketPayload.Type type, StreamCodec codec, BiConsumer handler) {
            HANDLED_TO_CLIENT_LIST.add(new HandledToClient<>(type, codec, handler));
        }

        @Override
        public void registerConfigToServer(CustomPacketPayload.Type type, StreamCodec codec, Consumer handler) {
            HANDLED_CONFIG_TO_SERVER_LIST.add(new HandledConfig<>(type, codec, handler));
        }

        @Override
        public void registerConfigToClient(CustomPacketPayload.Type type, StreamCodec codec, Consumer handler) {
            HANDLED_CONFIG_TO_CLIENT_LIST.add(new HandledConfig<>(type, codec, handler));
        }

        @SubscribeEvent
        public static void registerWithHandler(RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar("1");

            for (HandledToServer handled : HANDLED_TO_SERVER_LIST) {
                registrar.playToServer(
                        handled.type,
                        handled.codec,
                        (payload, context) -> handled.handler.accept(payload, context.player())
                );
            }

            for (HandledToClient handled : HANDLED_TO_CLIENT_LIST) {
                registrar.playToClient(
                        handled.type,
                        handled.codec,
                        (payload, context) -> handled.handler.accept(payload, context.player())
                );
            }

            for (HandledConfig handled : HANDLED_CONFIG_TO_SERVER_LIST) {
                registrar.configurationToServer(handled.type, handled.codec,
                        (payload, context) -> handled.handler.accept(payload));
            }

            for (HandledConfig handled : HANDLED_CONFIG_TO_CLIENT_LIST) {
                registrar.configurationToClient(handled.type, handled.codec,
                        (payload, context) -> handled.handler.accept(payload));
            }
        }
    }

    public static class ReloadListeners implements CommonHelpers.ReloadListeners {

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
        public static void addServerReloadListeners(final AddServerReloadListenersEvent event) {
            LISTENERS.forEach(pair -> event.addListener(pair.getFirst(), pair.getSecond()));
            ORDERING.forEach(pair -> event.addDependency(pair.getFirst(), pair.getSecond()));
        }
    }

    public static class DataRegistries implements CommonHelpers.DataRegistries {

        private static final List<Consumer<DataPackRegistryEvent.NewRegistry>> REGISTRATIONS = new ArrayList<>();

        @Override
        public <T> void register(ResourceKey<Registry<T>> key, Codec<T> codec) {
            REGISTRATIONS.add(event -> event.dataPackRegistry(key, codec));
        }

        @Override
        public <T> void registerSynced(ResourceKey<Registry<T>> key, Codec<T> serverCodec, Codec<T> clientCodec) {
            REGISTRATIONS.add(event -> event.dataPackRegistry(key, serverCodec, clientCodec));
        }

        @SubscribeEvent
        public static void registerDataRegistries(final DataPackRegistryEvent.NewRegistry event) {
            REGISTRATIONS.forEach(consumer -> consumer.accept(event));
        }
    }

    public static class EntityDataSerializers implements CommonHelpers.EntityDataSerializers {

        @Override
        public void register(Identifier id, Supplier<EntityDataSerializer<?>> serializer) {
            UnifiedRegistries.DeferredRegistry.create(id.getNamespace(), NeoForgeRegistries.ENTITY_DATA_SERIALIZERS).register(id.getPath(), serializer);
        }
    }

    public static class SpawnPlacements implements CommonHelpers.SpawnPlacements {

        private static final List<Consumer<RegisterSpawnPlacementsEvent>> REGISTRATIONS = new ArrayList<>();

        @Override
        public <T extends Mob> void register(Supplier<EntityType<T>> type, SpawnPlacementType placementType, Heightmap.Types heightmap, net.minecraft.world.entity.SpawnPlacements.SpawnPredicate<T> spawnPredicate) {
            REGISTRATIONS.add(event -> event.register(type.get(), placementType, heightmap, spawnPredicate, RegisterSpawnPlacementsEvent.Operation.OR));
        }

        @SubscribeEvent
        public static void registerSpawnPlacements(final RegisterSpawnPlacementsEvent event) {
            REGISTRATIONS.forEach(consumer -> consumer.accept(event));
        }
    }
}
