package net.rebel459.unified.platform;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.rebel459.unified.util.PackType;
import net.rebel459.unified.util.LoaderType;
import net.rebel459.unified.util.event.BiomeModificationContext;
import net.rebel459.unified.util.event.BiomeModificationsImpl;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class HelpersImpl {

    public interface Platform {

        LoaderType getLoader();

        boolean isClientSide();
        boolean isServerSide();

        boolean isModLoaded(String modId);

        boolean isDevelopmentEnvironment();
    }

    public interface CreativeEntries {

        void insert(ResourceKey<CreativeModeTab> tab, ItemLike... items);
        void insert(ResourceKey<CreativeModeTab> tab, ItemStackTemplate... items);
        void insert(List<ResourceKey<CreativeModeTab>> tabs, ItemLike... items);
        void insert(List<ResourceKey<CreativeModeTab>> tabs, ItemStackTemplate... items);
        void insertAfter(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemLike... addedItems);
        void insertAfter(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemStackTemplate... addedItems);
        void insertAfter(List<ResourceKey<CreativeModeTab>> tabs, ItemLike existingItem, ItemLike... addedItems);
        void insertAfter(List<ResourceKey<CreativeModeTab>> tabs, ItemLike existingItem, ItemStackTemplate... addedItems);
        void insertBefore(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemLike... addedItems);
        void insertBefore(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemStackTemplate... addedItems);
        void insertBefore(List<ResourceKey<CreativeModeTab>> tabs, ItemLike existingItem, ItemLike... addedItems);
        void insertBefore(List<ResourceKey<CreativeModeTab>> tabs, ItemLike existingItem, ItemStackTemplate... addedItems);
    }

    public interface Packs {

        void add(Identifier id, PackType type);
    }

    public interface Networking {

        <T extends CustomPacketPayload> void registerPlayToServer(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec);
        <T extends CustomPacketPayload> void registerPlayToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec);
        <T extends CustomPacketPayload> void registerConfigToServer(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec);
        <T extends CustomPacketPayload> void registerConfigToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec);

        <T extends CustomPacketPayload> void registerPlayToServer(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiConsumer<T, ServerPlayer> handler);
        <T extends CustomPacketPayload> void registerPlayToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiConsumer<T, Player> handler);
        <T extends CustomPacketPayload> void registerConfigToServer(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec, BiConsumer<T, ServerPlayer> handler);
        <T extends CustomPacketPayload> void registerConfigToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec, BiConsumer<T, Player> handler);

        boolean canSend(CustomPacketPayload payload, ServerPlayer player);
        void send(CustomPacketPayload payload, ServerPlayer player);
    }

    @Deprecated
    public interface BiomeModifications {
        final class Context extends BiomeModificationContext {
            private Context() {}
        }

        default void register(ResourceKey<Biome> biome, Consumer<Context> modifier) {
            BiomeModificationsImpl.register(biome, context -> modifier.accept(new Context()));
        }

        default void register(List<ResourceKey<Biome>> biomes, Consumer<Context> modifier) {
            BiomeModificationsImpl.register(biomes, context -> modifier.accept(new Context()));
        }

        default void register(TagKey<Biome> biome, Consumer<Context> modifier) {
            BiomeModificationsImpl.register(biome, context -> modifier.accept(new Context()));
        }
    }

    public interface ReloadListeners {

        void addListener(Identifier id, PreparableReloadListener listener);
        void addOrdering(Identifier first, Identifier second);
    }

    public interface DataRegistries {

        <T> void register(ResourceKey<Registry<T>> key, Codec<T> codec);
        <T> void registerSynced(ResourceKey<Registry<T>> key, Codec<T> serverCodec, Codec<T> clientCodec);
    }

    public interface EntityData {
        void registerSerializer(Identifier id, Supplier<EntityDataSerializer<?>> serializer);
    }
}
