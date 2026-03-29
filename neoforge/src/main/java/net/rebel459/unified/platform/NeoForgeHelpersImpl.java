package net.rebel459.unified.platform;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handlers.ServerPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.rebel459.unified.Unified;
import net.rebel459.unified.util.*;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NeoForgeHelpersImpl {

    public static class CreativeEntries implements HelpersImpl.CreativeEntries {

        private static List<Pair<ItemStackTemplate, ResourceKey<CreativeModeTab>>> INSERT_ITEMS = new ArrayList<>();
        private static List<Triple<ItemLike, ItemStackTemplate, ResourceKey<CreativeModeTab>>> INSERT_AFTER_ITEMS = new ArrayList<>();
        private static List<Triple<ItemLike, ItemStackTemplate, ResourceKey<CreativeModeTab>>> INSERT_BEFORE_ITEMS = new ArrayList<>();

        @Override
        public final void insert(ResourceKey<CreativeModeTab> tab, ItemLike... items) {
            var itemList = Arrays.stream(items).toList();
            for (ItemLike itemLike : itemList) {
                insert(tab, new ItemStackTemplate(itemLike.asItem()));
            }
        }

        @Override
        public void insert(ResourceKey<CreativeModeTab> tab, ItemStackTemplate... items) {
            List<ItemStackTemplate> itemList = Arrays.stream(items).toList();
            for (ItemStackTemplate item : itemList) {
                INSERT_ITEMS.add(Pair.of(item, tab));
            }
        }

        @Override
        public void insert(List<ResourceKey<CreativeModeTab>> tabs, ItemLike... items) {
            for (ResourceKey<CreativeModeTab> tab : tabs) {
                insert(tab, items);
            }
        }

        @Override
        public void insert(List<ResourceKey<CreativeModeTab>> tabs, ItemStackTemplate... items) {
            for (ResourceKey<CreativeModeTab> tab : tabs) {
                insert(tab, items);
            }
        }

        @Override
        public final void insertAfter(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemLike... addedItems) {
            var itemList = Arrays.stream(addedItems).toList();
            for (ItemLike itemLike : itemList) {
                insertAfter(tab, existingItem, new ItemStackTemplate(itemLike.asItem()));
            }
        }

        @Override
        public void insertAfter(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemStackTemplate... addedItems) {
            List<ItemStackTemplate> itemList = Arrays.stream(addedItems).toList();
            for (ItemStackTemplate addedItem : itemList) {
                INSERT_AFTER_ITEMS.add(Triple.of(existingItem, addedItem, tab));
            }
        }

        @Override
        public void insertAfter(List<ResourceKey<CreativeModeTab>> tabs, ItemLike existingItem, ItemLike... addedItems) {
            for (ResourceKey<CreativeModeTab> tab : tabs) {
                insertAfter(tab, existingItem, addedItems);
            }
        }

        @Override
        public void insertAfter(List<ResourceKey<CreativeModeTab>> tabs, ItemLike existingItem, ItemStackTemplate... addedItems) {
            for (ResourceKey<CreativeModeTab> tab : tabs) {
                insertAfter(tab, existingItem, addedItems);
            }
        }

        @Override
        public final void insertBefore(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemLike... addedItems) {
            var itemList = Arrays.stream(addedItems).toList();
            for (ItemLike itemLike : itemList) {
                insertBefore(tab, existingItem, new ItemStackTemplate(itemLike.asItem()));
            }
        }

        @Override
        public void insertBefore(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemStackTemplate... addedItems) {
            List<ItemStackTemplate> itemList = Arrays.stream(addedItems).toList();
            for (ItemStackTemplate addedItem : itemList) {
                INSERT_BEFORE_ITEMS.add(Triple.of(existingItem, addedItem, tab));
            }
        }

        @Override
        public void insertBefore(List<ResourceKey<CreativeModeTab>> tabs, ItemLike existingItem, ItemLike... addedItems) {
            for (ResourceKey<CreativeModeTab> tab : tabs) {
                insertBefore(tab, existingItem, addedItems);
            }
        }

        @Override
        public void insertBefore(List<ResourceKey<CreativeModeTab>> tabs, ItemLike existingItem, ItemStackTemplate... addedItems) {
            for (ResourceKey<CreativeModeTab> tab : tabs) {
                insertBefore(tab, existingItem, addedItems);
            }
        }

        @SubscribeEvent
        public static void buildContents(BuildCreativeModeTabContentsEvent event) {
            for (Pair<ItemStackTemplate, ResourceKey<CreativeModeTab>> pair : INSERT_ITEMS) {
                ItemStack item = pair.getFirst().create();
                ResourceKey<CreativeModeTab> tab = pair.getSecond();
                if (event.getTabKey().equals(tab)) {
                    event.accept(item);
                }
            }
            for (int x = INSERT_AFTER_ITEMS.size() - 1; x >= 0; x--) {
                var triple = INSERT_AFTER_ITEMS.get(x);
                ItemLike existingItem = triple.getLeft();
                ItemStack addedItem = triple.getMiddle().create();
                ResourceKey<CreativeModeTab> tab = triple.getRight();
                if (event.getTabKey().equals(tab)) {
                    event.insertAfter(existingItem.asItem().getDefaultInstance(), addedItem, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                }
            }
            for (Triple<ItemLike, ItemStackTemplate, ResourceKey<CreativeModeTab>> triple : INSERT_BEFORE_ITEMS) {
                ItemLike existingItem = triple.getLeft();
                ItemStack addedItem = triple.getMiddle().create();
                ResourceKey<CreativeModeTab> tab = triple.getRight();
                if (event.getTabKey().equals(tab)) {
                    event.insertBefore(existingItem.asItem().getDefaultInstance(), addedItem, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                }
            }
        }
    }

    public static class Packs implements HelpersImpl.Packs {

        public static List<Pair<Identifier, PackInfo>> PACK_LIST = new ArrayList<>();

        @Override
        public void add(Identifier id, PackInfo info) {
            PACK_LIST.add(Pair.of(id, info));
        }

        public static boolean getBoolean(PackInfo info) {
            return switch (info) {
                case REQUIRED_DATA, REQUIRED_RESOURCES -> true;
                case OPTIONAL_DATA, OPTIONAL_RESOURCES -> false;
            };
        }

        public static PackType getType(PackInfo info) {
            return switch (info) {
                case REQUIRED_DATA, OPTIONAL_DATA -> PackType.SERVER_DATA;
                case REQUIRED_RESOURCES, OPTIONAL_RESOURCES -> PackType.CLIENT_RESOURCES;
            };
        }

        @SubscribeEvent
        public static void addFeaturePacks(AddPackFindersEvent event) {
            for (Pair<Identifier, PackInfo> pair : PACK_LIST) {
                Identifier id = pair.getFirst();
                PackInfo info = pair.getSecond();

                event.addPackFinders(
                        Identifier.fromNamespaceAndPath(id.getNamespace(), "resourcepacks/" + id.getPath()),
                        getType(info),
                        Component.translatable("pack." + id.getNamespace() + "." + id.getPath()),
                        PackSource.BUILT_IN,
                        getBoolean(info),
                        Pack.Position.TOP
                );
            }
        }
    }

    public static class Networking implements HelpersImpl.Networking {

        @Override
        public void send(CustomPacketPayload payload, ServerPlayer player) {
            player.connection.send(new ClientboundCustomPayloadPacket(payload));
        }

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

        @SubscribeEvent
        public static void register(RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar("1");
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

        private record HandledToServer<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type, StreamCodec<FriendlyByteBuf, T> codec, BiConsumer<T, ServerPlayer> handler, boolean play) {}
        private record HandledToClient<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type, StreamCodec<FriendlyByteBuf, T> codec, BiConsumer<T, Player> handler, boolean play) {}

        @Override
        public void registerPlayToServer(CustomPacketPayload.Type type, StreamCodec codec, BiConsumer handler) {
            HANDLED_TO_SERVER_LIST.add(new HandledToServer<>(type, codec, handler, true));
        }

        @Override
        public void registerPlayToClient(CustomPacketPayload.Type type, StreamCodec codec, BiConsumer handler) {
            HANDLED_TO_CLIENT_LIST.add(new HandledToClient<>(type, codec, handler, true));
        }

        @Override
        public void registerConfigToServer(CustomPacketPayload.Type type, StreamCodec codec, BiConsumer handler) {
            HANDLED_TO_SERVER_LIST.add(new HandledToServer<>(type, codec, handler, false));
        }

        @Override
        public void registerConfigToClient(CustomPacketPayload.Type type, StreamCodec codec, BiConsumer handler) {
            HANDLED_TO_CLIENT_LIST.add(new HandledToClient<>(type, codec, handler, false));
        }

        @SubscribeEvent
        public static void registerWithHandler(RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar("1");

            for (HandledToServer handled : HANDLED_TO_SERVER_LIST) {

                if (handled.play) {
                    registrar.playToServer(
                            handled.type,
                            handled.codec,
                            (payload, context) -> {
                                handled.handler.accept(payload, context.player());
                            }
                    );
                } else {
                    registrar.configurationToServer(
                            handled.type,
                            handled.codec,
                            (payload, context) -> {
                                handled.handler.accept(payload, context.player());
                            }
                    );
                }
            }

            for (HandledToClient handled : HANDLED_TO_CLIENT_LIST) {

                if (handled.play) {
                    registrar.playToClient(
                            handled.type,
                            handled.codec,
                            (payload, context) -> handled.handler.accept(payload, context.player())
                    );
                } else {
                    registrar.configurationToClient(
                            handled.type,
                            handled.codec,
                            (payload, context) -> handled.handler.accept(payload, context.player())
                    );
                }
            }
        }
    }

    public static class BiomeModifications implements HelpersImpl.BiomeModifications {

        public static final List<BiomeModifier> MODIFIERS = new ArrayList<>();

        private static class WorldgenBuilder implements Worldgen {

            private record AddFeatureAction(ResourceKey<PlacedFeature> feature, GenerationStep.Decoration step) {}
            private record RemoveFeatureAction(ResourceKey<PlacedFeature> feature, GenerationStep.Decoration step) {}

            private final HolderSet<Biome> targetBiomes;
            private final List<AddFeatureAction> toAddFeature = new ArrayList<>();
            private final List<RemoveFeatureAction> toRemoveFeature = new ArrayList<>();
            private final List<ResourceKey<ConfiguredWorldCarver<?>>> toAddCarver = new ArrayList<>();
            private final List<ResourceKey<ConfiguredWorldCarver<?>>> toRemoveCarver = new ArrayList<>();

            WorldgenBuilder(HolderSet<Biome> target) { this.targetBiomes = target; }

            @Override
            public void addFeature(ResourceKey<PlacedFeature> feature, GenerationStep.Decoration step) {
                toAddFeature.add(new AddFeatureAction(feature, step));
            }

            @Override
            public void removeFeature(ResourceKey<PlacedFeature> feature, GenerationStep.Decoration step) {
                toRemoveFeature.add(new RemoveFeatureAction(feature, step));
            }

            @Override
            public void addCarver(ResourceKey<ConfiguredWorldCarver<?>> carverKey) {
                toAddCarver.add(carverKey);
            }

            @Override
            public void removeCarver(ResourceKey<ConfiguredWorldCarver<?>> carverKey) {
                toRemoveCarver.add(carverKey);
            }

            void build(HolderLookup.Provider lookup) {
                for (var entry : toAddFeature) {
                    MODIFIERS.add(new BiomeModifiers.AddFeaturesBiomeModifier(targetBiomes, HolderSet.direct(lookup.lookup(Registries.PLACED_FEATURE).get().get(entry.feature).get().getDelegate()), entry.step));
                }
                for (var entry : toRemoveFeature) {
                    MODIFIERS.add(new BiomeModifiers.RemoveFeaturesBiomeModifier(targetBiomes, HolderSet.direct(lookup.lookup(Registries.PLACED_FEATURE).get().get(entry.feature).get().getDelegate()), Set.of(entry.step)));
                }
                for (var entry : toAddCarver) {
                    MODIFIERS.add(new BiomeModifiers.AddCarversBiomeModifier(targetBiomes, HolderSet.direct(lookup.lookup(Registries.CONFIGURED_CARVER).get().get(entry).get().getDelegate())));
                }
                for (var entry : toRemoveCarver) {
                    MODIFIERS.add(new BiomeModifiers.RemoveCarversBiomeModifier(targetBiomes, HolderSet.direct(lookup.lookup(Registries.CONFIGURED_CARVER).get().get(entry).get().getDelegate())));
                }
            }
        }

        private static class EffectsBuilder implements BiomeModifications.Effects {
            private final HolderSet<Biome> targetBiomes;
            private Integer waterColor = null;
            private Integer foliageColor = null;
            private Integer dryFoliageColor = null;
            private Integer grassColor = null;

            EffectsBuilder(HolderSet<Biome> target) { this.targetBiomes = target; }

            @Override
            public void setWaterColor(int color) {
                this.waterColor = color;
            }

            @Override
            public void setFoliageColor(int color) {
                this.foliageColor = color;
            }

            @Override
            public void setDryFoliageColor(int color) {
                this.dryFoliageColor = color;
            }

            @Override
            public void setGrassColor(int color) {
                this.grassColor = color;
            }

            void build() {
                if (waterColor != null) {
                    MODIFIERS.add(new UnifiedBiomeModifiers.SetEffectModifier(targetBiomes, UnifiedBiomeModifiers.EffectType.WATER, waterColor));
                }
                if (foliageColor != null) {
                    MODIFIERS.add(new UnifiedBiomeModifiers.SetEffectModifier(targetBiomes, UnifiedBiomeModifiers.EffectType.FOLIAGE, foliageColor));
                }
                if (dryFoliageColor != null) {
                    MODIFIERS.add(new UnifiedBiomeModifiers.SetEffectModifier(targetBiomes, UnifiedBiomeModifiers.EffectType.DRY_FOLIAGE, dryFoliageColor));
                }
                if (grassColor != null) {
                    MODIFIERS.add(new UnifiedBiomeModifiers.SetEffectModifier(targetBiomes, UnifiedBiomeModifiers.EffectType.GRASS, grassColor));
                }
            }
        }

        private static class ClimateBuilder implements HelpersImpl.BiomeModifications.Climate {
            private final HolderSet<Biome> targetBiomes;
            private float temperature;
            private boolean changedTemperature = false;
            private float downfall;
            private boolean changedDownfall = false;
            private boolean hasPrecipitation;
            private boolean changedHasPrecipitation = false;

            ClimateBuilder(HolderSet<Biome> target) {this.targetBiomes = target;}

            @Override
            public void setTemperature(float temperature) {
                this.temperature = temperature;
                this.changedTemperature = true;
            }

            @Override
            public void setDownfall(float downfall) {
                this.downfall = downfall;
                this.changedDownfall = true;
            }

            @Override
            public void setPrecipitation(boolean hasPrecipitation) {
                this.hasPrecipitation = hasPrecipitation;
                this.changedHasPrecipitation = true;
            }

            void build() {
                if (changedTemperature) {
                    MODIFIERS.add(new UnifiedBiomeModifiers.SetClimateModifier(this.targetBiomes, UnifiedBiomeModifiers.ClimateType.TEMPERATURE, temperature));
                }
                if (changedDownfall) {
                    MODIFIERS.add(new UnifiedBiomeModifiers.SetClimateModifier(this.targetBiomes, UnifiedBiomeModifiers.ClimateType.DOWNFALL, downfall));
                }
                if (changedHasPrecipitation) {
                    MODIFIERS.add(new UnifiedBiomeModifiers.SetPrecipitationModifier(this.targetBiomes, hasPrecipitation));
                }
            }
        }

        private static class EnvironmentAttributesBuilder implements BiomeModifications.EnvironmentAttributes {
            private record SetAction(EnvironmentAttribute attribute, Object value) {}

            private final HolderSet<Biome> targetBiomes;
            private final List<EnvironmentAttributesBuilder.SetAction> toSet = new ArrayList<>();

            EnvironmentAttributesBuilder(HolderSet<Biome> target) { this.targetBiomes = target; }

            @Override
            public <Value> void set(EnvironmentAttribute<Value> attribute, Value value) {
                toSet.add(new SetAction(attribute, value));
            }

            void build() {
                for (var entry : toSet) {
                    MODIFIERS.add(new UnifiedBiomeModifiers.SetEnvironmentAttributeModifier(targetBiomes, entry.attribute, entry.value));
                }
            }
        }

        private static class MobSpawnsBuilder implements HelpersImpl.BiomeModifications.MobSpawns {

            private record AddSpawn(MobSpawnSettings.SpawnerData data, int weight) {}
            private record RemoveSpawn(EntityType<?> entityType) {}
            private record AddCharge(EntityType<?> entityType, double charge, double energyBudget) {}
            private record RemoveCharge(EntityType<?> entityType) {}

            private final HolderSet<Biome> targetBiomes;
            private final List<MobSpawnsBuilder.AddSpawn> toAddSpawn = new ArrayList<>();
            private final List<MobSpawnsBuilder.RemoveSpawn> toRemoveSpawn = new ArrayList<>();
            private final List<MobSpawnsBuilder.AddCharge> toAddCharge = new ArrayList<>();
            private final List<MobSpawnsBuilder.RemoveCharge> toRemoveCharge = new ArrayList<>();

            MobSpawnsBuilder(HolderSet<Biome> target) { this.targetBiomes = target; }

            @Override
            public void addSpawn(MobSpawnSettings.SpawnerData data, int weight) {
                toAddSpawn.add(new AddSpawn(data, weight));
            }

            @Override
            public void removeSpawn(EntityType<?> entityType) {
                toRemoveSpawn.add(new RemoveSpawn(entityType));
            }

            @Override
            public void addCharge(EntityType<?> entityType, double charge, double energyBudget) {
                toAddCharge.add(new AddCharge(entityType, charge, energyBudget));
            }

            @Override
            public void removeCharge(EntityType<?> entityType) {
                toRemoveCharge.add(new RemoveCharge(entityType));
            }

            void build(HolderLookup.Provider lookup) {
                for (var entry : toAddSpawn) {
                    MODIFIERS.add(new BiomeModifiers.AddSpawnsBiomeModifier(targetBiomes, WeightedList.<MobSpawnSettings.SpawnerData>builder().add(entry.data, entry.weight).build()));
                }
                for (var entry : toRemoveSpawn) {
                    MODIFIERS.add(new BiomeModifiers.RemoveSpawnsBiomeModifier(targetBiomes, HolderSet.direct(lookup.lookup(Registries.ENTITY_TYPE).get().get(entry.entityType.builtInRegistryHolder().key()).get().getDelegate())));
                }
                for (var entry : toAddCharge) {
                    MODIFIERS.add(new BiomeModifiers.AddSpawnCostsBiomeModifier(targetBiomes, HolderSet.direct(lookup.lookup(Registries.ENTITY_TYPE).get().get(entry.entityType.builtInRegistryHolder().key()).get().getDelegate()), new MobSpawnSettings.MobSpawnCost(entry.energyBudget, entry.charge)));
                }
                for (var entry : toRemoveCharge) {
                    MODIFIERS.add(new BiomeModifiers.RemoveSpawnCostsBiomeModifier(targetBiomes, HolderSet.direct(lookup.lookup(Registries.ENTITY_TYPE).get().get(entry.entityType.builtInRegistryHolder().key()).get().getDelegate())));
                }
            }
        }

        public void doRegister(HolderSet<Biome> set, Consumer<Context> consumer, HolderLookup.Provider lookup) {
            var worldgen = new WorldgenBuilder(set);
            var effects  = new EffectsBuilder(set);
            var climate  = new ClimateBuilder(set);
            var environmentAttributes = new EnvironmentAttributesBuilder(set);
            var spawns = new MobSpawnsBuilder(set);

            Context context = new Context(worldgen, effects, climate, environmentAttributes, spawns);
            consumer.accept(context);

            worldgen.build(lookup);
            effects.build();
            climate.build();
            environmentAttributes.build();
            spawns.build(lookup);
        }

        @Override
        public void register(ResourceKey<Biome> biome, Consumer<Context> consumer) {
            BiomeBuilderEvent.onRunModifiers(lookup -> {
            Holder<Biome> holder = lookup.lookup(Registries.BIOME).get().getOrThrow(biome);
            HolderSet<Biome> set = HolderSet.direct(holder);
                doRegister(set, consumer, lookup);
            });
        }

        @Override
        public void register(List<ResourceKey<Biome>> biomes, Consumer<Context> consumer) {
            BiomeBuilderEvent.onRunModifiers(lookup -> {
                List<Holder<Biome>> holders = biomes.stream()
                        .map(key -> lookup.lookup(Registries.BIOME).get().getOrThrow(key).getDelegate())
                        .toList();
                HolderSet<Biome> set = HolderSet.direct(holders);
                doRegister(set, consumer, lookup);
            });
        }

        @Override
        public void register(TagKey<Biome> tag, Consumer<Context> consumer) {
            BiomeBuilderEvent.onRunModifiers(lookup -> {
                HolderSet<Biome> set = lookup.lookup(Registries.BIOME).get().getOrThrow(tag);
                doRegister(set, consumer, lookup);
            });
        }
    }
}