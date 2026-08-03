package net.rebel459.unified.platform;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.rebel459.unified.util.PackType;
import net.rebel459.unified.util.LoaderType;
import net.rebel459.unified.util.VanillaVersion;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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

    public interface BiomeModifications {

        interface Worldgen {
            void addFeature(ResourceKey<PlacedFeature> feature, GenerationStep.Decoration step);
            void removeFeature(ResourceKey<PlacedFeature> feature, GenerationStep.Decoration step);
            void addCarver(ResourceKey<ConfiguredWorldCarver<?>> carverKey);
            void removeCarver(ResourceKey<ConfiguredWorldCarver<?>> carverKey);
        }

        interface Effects {
            void setWaterColor(int color);
            void setFoliageColor(int color);
            void setDryFoliageColor(int color);
            void setGrassColor(int color);
        }

        interface Climate {
            void setTemperature(float temperature);
            void setDownfall(float downfall);
            void setPrecipitation(boolean hasPrecipitation);
        }

        interface EnvironmentAttributes {
            <Value> void set(EnvironmentAttribute<Value> attribute, Value value);
        }

        interface MobSpawns {
            void addSpawn(MobSpawnSettings.SpawnerData data, int weight);
            void removeSpawn(EntityType<?> entityType);

            void addCharge(EntityType<?> entityType, double charge, double energyBudget);
            void removeCharge(EntityType<?> entityType);
        }

        final class Context {
            private final Worldgen worldgen;
            private final Effects effects;
            private final Climate climate;
            private final EnvironmentAttributes environmentAttributes;
            private final MobSpawns mobSpawns;

            Context(Worldgen worldgen, Effects effects, Climate climate, EnvironmentAttributes environmentAttributes, MobSpawns mobSpawns) {
                this.worldgen = worldgen;
                this.effects = effects;
                this.climate = climate;
                this.environmentAttributes = environmentAttributes;
                this.mobSpawns = mobSpawns;
            }

            public Worldgen getFeatures() {
                return worldgen;
            }

            public Effects getEffects() {
                return effects;
            }

            public Climate getClimate() {
                return climate;
            }

            public EnvironmentAttributes getEnvironmentAttributes() {
                return environmentAttributes;
            }

            public MobSpawns getMobSpawns() {
                return mobSpawns;
            }
        }

        void register(ResourceKey<Biome> biome, Consumer<Context> context);
        void register(List<ResourceKey<Biome>> biomes, Consumer<Context> context);
        void register(TagKey<Biome> biome, Consumer<Context> context);
    }
}