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
import net.rebel459.unified.util.data.BiomeModifiers;
import net.rebel459.unified.util.event.BiomeModificationContext;

import java.util.*;
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

        private static BiomeModificationContext context(Object helper) {
            if (helper instanceof Context.Bound bound) return bound.biomeContext();
            throw new IllegalStateException("Biome modification helper used outside its Context");
        }

        final class Context {
            private final BiomeModificationContext context;
            private final Adapter adapter = new Adapter();

            private Context(BiomeModificationContext context) {
                this.context = context;
            }

            public Worldgen getFeatures() { return adapter; }
            public Effects getEffects() { return adapter; }
            public Climate getClimate() { return adapter; }
            public EnvironmentAttributes getEnvironmentAttributes() { return adapter; }
            public MobSpawns getMobSpawns() { return adapter; }

            private interface Bound {
                BiomeModificationContext biomeContext();
            }

            private final class Adapter implements Bound, Worldgen, Effects, Climate, EnvironmentAttributes, MobSpawns {
                @Override
                public BiomeModificationContext biomeContext() { return context; }
            }
        }

        interface Worldgen {
            default void addFeature(ResourceKey<PlacedFeature> feature, GenerationStep.Decoration step) {
                BiomeModificationContext context = HelpersImpl.BiomeModifications.context(this);
                context.getFeatures().addFeature(feature, step);
            }

            default void removeFeature(ResourceKey<PlacedFeature> feature, GenerationStep.Decoration step) {
                BiomeModificationContext context = HelpersImpl.BiomeModifications.context(this);
                context.getFeatures().removeFeature(feature, step);
            }

            default void addCarver(ResourceKey<ConfiguredWorldCarver<?>> carver) {
                BiomeModificationContext context = HelpersImpl.BiomeModifications.context(this);
                context.getFeatures().addCarver(carver);
            }

            default void removeCarver(ResourceKey<ConfiguredWorldCarver<?>> carver) {
                BiomeModificationContext context = HelpersImpl.BiomeModifications.context(this);
                context.getFeatures().removeCarver(carver);
            }
        }

        interface Effects {
            default void setWaterColor(int color) {
                BiomeModificationContext context = HelpersImpl.BiomeModifications.context(this);
                context.getEffects().setWaterColor(color);
            }

            default void setFoliageColor(int color) {
                BiomeModificationContext context = HelpersImpl.BiomeModifications.context(this);
                context.getEffects().setFoliageColor(color);
            }

            default void setDryFoliageColor(int color) {
                BiomeModificationContext context = HelpersImpl.BiomeModifications.context(this);
                context.getEffects().setDryFoliageColor(color);
            }

            default void setGrassColor(int color) {
                BiomeModificationContext context = HelpersImpl.BiomeModifications.context(this);
                context.getEffects().setGrassColor(color);
            }
        }

        interface Climate {
            default void setTemperature(float temperature) {
                BiomeModificationContext context = HelpersImpl.BiomeModifications.context(this);
                context.getClimate().setTemperature(temperature);
            }

            default void setDownfall(float downfall) {
                BiomeModificationContext context = HelpersImpl.BiomeModifications.context(this);
                context.getClimate().setDownfall(downfall);
            }

            default void setPrecipitation(boolean hasPrecipitation) {
                BiomeModificationContext context = HelpersImpl.BiomeModifications.context(this);
                context.getClimate().setPrecipitation(hasPrecipitation);
            }
        }

        interface EnvironmentAttributes {
            default <Value> void set(EnvironmentAttribute<Value> attribute, Value value) {
                BiomeModificationContext context = HelpersImpl.BiomeModifications.context(this);
                context.getEnvironmentAttributes().set(attribute, value);
            }
        }

        interface MobSpawns {
            default void addSpawn(MobSpawnSettings.SpawnerData data, int weight) {
                BiomeModificationContext context = HelpersImpl.BiomeModifications.context(this);
                context.getMobSpawns().addSpawn(data, weight);
            }

            default void removeSpawn(EntityType<?> entityType) {
                BiomeModificationContext context = HelpersImpl.BiomeModifications.context(this);
                context.getMobSpawns().removeSpawn(entityType);
            }

            default void addCharge(EntityType<?> entityType, double charge, double energyBudget) {
                BiomeModificationContext context = HelpersImpl.BiomeModifications.context(this);
                context.getMobSpawns().addCharge(entityType, charge, energyBudget);
            }

            default void removeCharge(EntityType<?> entityType) {
                BiomeModificationContext context = HelpersImpl.BiomeModifications.context(this);
                context.getMobSpawns().removeCharge(entityType);
            }
        }

        default void register(ResourceKey<Biome> biome, Consumer<Context> modifier) {
            BiomeModifiers.EVENT_ENTRIES.add(new BiomeModifiers.EventEntry(0, (reference, context) -> {
                if (reference.is(biome)) modifier.accept(new Context(context));
            }));
        }

        default void register(List<ResourceKey<Biome>> biomes, Consumer<Context> modifier) {
            List<ResourceKey<Biome>> targets = List.copyOf(biomes);
            BiomeModifiers.EVENT_ENTRIES.add(new BiomeModifiers.EventEntry(0, (reference, context) -> {
                if (targets.contains(reference.key())) modifier.accept(new Context(context));
            }));
        }

        default void register(TagKey<Biome> biomes, Consumer<Context> modifier) {
            BiomeModifiers.EVENT_ENTRIES.add(new BiomeModifiers.EventEntry(0, (reference, context) -> {
                if (reference.is(biomes)) modifier.accept(new Context(context));
            }));
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
