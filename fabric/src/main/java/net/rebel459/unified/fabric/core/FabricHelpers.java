package net.rebel459.unified.fabric.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.helper.BiomeModificationContext;
import net.rebel459.unified.impl.core.CommonHelpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FabricHelpers {

    public static class CreativeEntries implements CommonHelpers.CreativeEntries {

        @Override
        public final void insert(ResourceKey<CreativeModeTab> tab, ItemLike... items) {
            var itemList = Arrays.stream(items).toList();
            for (ItemLike itemLike : itemList) {
                insert(tab, new ItemStackTemplate(itemLike.asItem()));
            }
        }

        @SafeVarargs
        @Override
        public final void insert(ResourceKey<CreativeModeTab> tab, ItemStackTemplate... items) {
            var itemList = Arrays.stream(items).toList();
            for (int x = itemList.size() - 1; x >= 0; x--) {
                ItemStackTemplate template = itemList.get(x);
                CreativeModeTabEvents.modifyOutputEvent(tab).register(entries -> {
                    entries.accept(template.create());
                });
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
            for (int x = itemList.size() - 1; x >= 0; x--) {
                insertAfter(tab, existingItem, new ItemStackTemplate(itemList.get(x).asItem()));
            }
        }

        @Override
        public void insertAfter(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemStackTemplate... addedItems) {
            CreativeModeTabEvents.modifyOutputEvent(tab).register(entries -> {
                List<ItemStackTemplate> itemList = Arrays.stream(addedItems).toList();
                for (int x = itemList.size() - 1; x >= 0; x--) {
                    entries.insertAfter(existingItem, itemList.get(x).create());
                }
            });
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
            CreativeModeTabEvents.modifyOutputEvent(tab).register(entries -> {
                List<ItemStackTemplate> itemList = Arrays.stream(addedItems).toList();
                ItemStackTemplate previousTemplate = null;
                for (ItemStackTemplate template : itemList) {
                    if (previousTemplate == null) {
                        entries.insertBefore(existingItem, template.create());
                    }
                    else {
                        entries.insertBefore(previousTemplate.create(), template.create());
                    }
                    previousTemplate = template;
                }
            });
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
    }

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
        public void registerConfigToServer(CustomPacketPayload.Type type, StreamCodec codec, BiConsumer handler) {

            PayloadTypeRegistry.serverboundConfiguration().register(type, codec);

            ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
                handler.accept(payload, context.player());
            });
        }

        @Override
        public void registerConfigToClient(CustomPacketPayload.Type type, StreamCodec codec, BiConsumer handler) {

            PayloadTypeRegistry.clientboundConfiguration().register(type, codec);

            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
                    handler.accept(payload, context.player());
                });
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

    public static class BiomeModifications implements CommonHelpers.BiomeModifications {

        private static int ID = 1;

        private static class WorldgenBuilder implements BiomeModificationContext.Worldgen {
            private record AddFeatureAction(ResourceKey<PlacedFeature> feature, GenerationStep.Decoration step) {}
            private record RemoveFeatureAction(ResourceKey<PlacedFeature> feature, GenerationStep.Decoration step) {}

            private final Predicate<BiomeSelectionContext> targetBiomes;
            private final List<AddFeatureAction> toAddFeature = new ArrayList<>();
            private final List<RemoveFeatureAction> toRemoveFeature = new ArrayList<>();
            private final List<ResourceKey<WorldCarver>> toAddCarver = new ArrayList<>();
            private final List<ResourceKey<WorldCarver>> toRemoveCarver = new ArrayList<>();

            WorldgenBuilder(Predicate<BiomeSelectionContext> target) { this.targetBiomes = target; }

            @Override
            public void addFeature(ResourceKey<PlacedFeature> feature, GenerationStep.Decoration step) {
                toAddFeature.add(new AddFeatureAction(feature, step));
            }

            @Override
            public void removeFeature(ResourceKey<PlacedFeature> feature, GenerationStep.Decoration step) {
                toRemoveFeature.add(new RemoveFeatureAction(feature, step));
            }

            @Override
            public void addCarver(ResourceKey<WorldCarver> carverKey) {
                toAddCarver.add(carverKey);
            }

            @Override
            public void removeCarver(ResourceKey<WorldCarver> carverKey) {
                toRemoveCarver.add(carverKey);
            }

            void build() {
                if (this.toAddFeature.isEmpty() && this.toRemoveFeature.isEmpty() && this.toAddCarver.isEmpty() && this.toRemoveCarver.isEmpty()) return;

                final var addFeatures = List.copyOf(this.toAddFeature);
                final var removeFeatures = List.copyOf(this.toRemoveFeature);
                final var addCarvers = List.copyOf(this.toAddCarver);
                final var removeCarvers = List.copyOf(this.toRemoveCarver);

                final var id = Identifier.fromNamespaceAndPath(Unified.MOD_ID, "unified_modifications_" + ID);

                net.fabricmc.fabric.api.biome.v1.BiomeModifications.create(id).add(
                        net.fabricmc.fabric.api.biome.v1.ModificationPhase.REPLACEMENTS,
                        this.targetBiomes,
                        (selectionContext, modificationContext) -> {
                            var generation = modificationContext.getGenerationSettings();

                            for (var action : removeFeatures) {
                                generation.removeFeature(action.step, action.feature);
                            }

                            for (var carver : removeCarvers) {
                                generation.removeCarver(carver);
                            }

                            for (var action : addFeatures) {
                                generation.removeFeature(action.step, action.feature);
                                generation.addFeature(action.step, action.feature);
                            }

                            for (var carver : addCarvers) {
                                generation.removeCarver(carver);
                                generation.addCarver(carver);
                            }
                        }
                );
            }
        }

        private static class EffectsBuilder implements BiomeModificationContext.Effects {
            private final Predicate<BiomeSelectionContext> targetBiomes;
            private Integer waterColor = null;
            private Integer foliageColor = null;
            private Integer dryFoliageColor = null;
            private Integer grassColor = null;

            EffectsBuilder(Predicate<BiomeSelectionContext> target) { this.targetBiomes = target; }

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
                    net.fabricmc.fabric.api.biome.v1.BiomeModifications.create(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "unified_modifications_" + ID)).add(
                            ModificationPhase.REPLACEMENTS,
                            (this.targetBiomes),
                            (selectionContext, modificationContext) -> {
                                modificationContext.getEffects().setWaterColor(waterColor);
                                ID += 1;
                            }
                    );
                }
                if (foliageColor != null) {
                    net.fabricmc.fabric.api.biome.v1.BiomeModifications.create(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "unified_modifications_" + ID)).add(
                            ModificationPhase.REPLACEMENTS,
                            (this.targetBiomes),
                            (selectionContext, modificationContext) -> {
                                modificationContext.getEffects().setFoliageColorOverride(foliageColor);
                                ID += 1;
                            }
                    );
                }
                if (dryFoliageColor != null) {
                    net.fabricmc.fabric.api.biome.v1.BiomeModifications.create(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "unified_modifications_" + ID)).add(
                            ModificationPhase.REPLACEMENTS,
                            (this.targetBiomes),
                            (selectionContext, modificationContext) -> {
                                modificationContext.getEffects().setDryFoliageColorOverride(dryFoliageColor);
                                ID += 1;
                            }
                    );
                }
                if (grassColor != null) {
                    net.fabricmc.fabric.api.biome.v1.BiomeModifications.create(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "unified_modifications_" + ID)).add(
                            ModificationPhase.REPLACEMENTS,
                            (this.targetBiomes),
                            (selectionContext, modificationContext) -> {
                                modificationContext.getEffects().setGrassColorOverride(grassColor);
                                ID += 1;
                            }
                    );
                }
            }
        }

        private static class ClimateBuilder implements BiomeModificationContext.Climate {
            private final Predicate<BiomeSelectionContext> targetBiomes;
            private float temperature;
            private boolean changedTemperature = false;
            private float downfall;
            private boolean changedDownfall = false;
            private boolean hasPrecipitation;
            private boolean changedHasPrecipitation = false;

            ClimateBuilder(Predicate<BiomeSelectionContext> target) {this.targetBiomes = target;}

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
                    net.fabricmc.fabric.api.biome.v1.BiomeModifications.create(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "unified_modifications_" + ID)).add(
                            ModificationPhase.REPLACEMENTS,
                            (this.targetBiomes),
                            (selectionContext, modificationContext) -> {
                                modificationContext.getWeather().setTemperature(temperature);
                                ID += 1;
                            }
                    );
                }
                if (changedDownfall) {
                    net.fabricmc.fabric.api.biome.v1.BiomeModifications.create(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "unified_modifications_" + ID)).add(
                            ModificationPhase.REPLACEMENTS,
                            (this.targetBiomes),
                            (selectionContext, modificationContext) -> {
                                modificationContext.getWeather().setDownfall(downfall);
                                ID += 1;
                            }
                    );
                }
                if (changedHasPrecipitation) {
                    net.fabricmc.fabric.api.biome.v1.BiomeModifications.create(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "unified_modifications_" + ID)).add(
                            ModificationPhase.REPLACEMENTS,
                            (this.targetBiomes),
                            (selectionContext, modificationContext) -> {
                                modificationContext.getWeather().setPrecipitation(hasPrecipitation);
                                ID += 1;
                            }
                    );
                }
            }
        }

        private static class EnvironmentAttributesBuilder implements BiomeModificationContext.EnvironmentAttributes {
            private record SetAction(EnvironmentAttribute attribute, Object value) {}

            private final Predicate<BiomeSelectionContext> targetBiomes;
            private final List<EnvironmentAttributesBuilder.SetAction> toSet = new ArrayList<>();

            EnvironmentAttributesBuilder(Predicate<BiomeSelectionContext> target) { this.targetBiomes = target; }

            @Override
            public <Value> void set(EnvironmentAttribute<Value> attribute, Value value) {
                toSet.add(new SetAction(attribute, value));
            }

            void build() {
                for (var entry : toSet) {
                    net.fabricmc.fabric.api.biome.v1.BiomeModifications.create(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "unified_modifications_" + ID)).add(
                            ModificationPhase.REPLACEMENTS,
                            (this.targetBiomes),
                            (selectionContext, modificationContext) -> {
                                modificationContext.getAttributes().set(entry.attribute, entry.value);
                                ID += 1;
                            }
                    );
                }
            }
        }

        private static class MobSpawnsBuilder implements BiomeModificationContext.MobSpawns {
            private record AddSpawn(MobSpawnSettings.SpawnerData data, int weight) {}
            private record AddCharge(EntityType<?> entityType, double charge, double energyBudget) {}

            private final Predicate<BiomeSelectionContext> targetBiomes;
            private final List<MobSpawnsBuilder.AddSpawn> toAddSpawn = new ArrayList<>();
            private final List<EntityType<?>> toRemoveSpawn = new ArrayList<>();
            private final List<MobSpawnsBuilder.AddCharge> toAddCharge = new ArrayList<>();
            private final List<EntityType<?>> toRemoveCharge = new ArrayList<>();

            MobSpawnsBuilder(Predicate<BiomeSelectionContext> target) { this.targetBiomes = target; }

            @Override
            public void addSpawn(MobSpawnSettings.SpawnerData data, int weight) {
                toAddSpawn.add(new AddSpawn(data, weight));
            }

            @Override
            public void removeSpawn(EntityType<?> entityType) {
                toRemoveSpawn.add(entityType);
            }

            @Override
            public void addCharge(EntityType<?> entityType, double charge, double energyBudget) {
                toAddCharge.add(new AddCharge(entityType, charge, energyBudget));
            }

            @Override
            public void removeCharge(EntityType<?> entityType) {
                toRemoveCharge.add(entityType);
            }

            void build() {
                for (var entry : toAddSpawn) {
                    net.fabricmc.fabric.api.biome.v1.BiomeModifications.create(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "unified_modifications_" + ID)).add(
                            ModificationPhase.ADDITIONS,
                            (this.targetBiomes),
                            (selectionContext, modificationContext) -> {
                                modificationContext.getMobSpawnSettings().addSpawn(
                                        entry.data.type().getCategory(),
                                        entry.data,
                                        entry.weight
                                );
                                ID += 1;
                            }
                    );
                }
                for (var entry : toRemoveSpawn) {
                    net.fabricmc.fabric.api.biome.v1.BiomeModifications.create(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "unified_modifications_" + ID)).add(
                            ModificationPhase.REMOVALS,
                            (this.targetBiomes),
                            (selectionContext, modificationContext) -> {
                                modificationContext.getMobSpawnSettings().removeSpawnsOfEntityType(
                                        entry
                                );
                                ID += 1;
                            }
                    );
                }
                for (var entry : toAddCharge) {
                    net.fabricmc.fabric.api.biome.v1.BiomeModifications.create(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "unified_modifications_" + ID)).add(
                            ModificationPhase.ADDITIONS,
                            (this.targetBiomes),
                            (selectionContext, modificationContext) -> {
                                modificationContext.getMobSpawnSettings().addMobCharge(
                                        entry.entityType,
                                        entry.charge,
                                        entry.energyBudget
                                );
                                ID += 1;
                            }
                    );                }
                for (var entry : toRemoveCharge) {
                    net.fabricmc.fabric.api.biome.v1.BiomeModifications.create(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "unified_modifications_" + ID)).add(
                            ModificationPhase.REMOVALS,
                            (this.targetBiomes),
                            (selectionContext, modificationContext) -> {
                                modificationContext.getMobSpawnSettings().clearMobCharge(
                                        entry
                                );
                                ID += 1;
                            }
                    );
                }
            }
        }

        public void doRegister(Predicate<BiomeSelectionContext> selection, Consumer<BiomeModificationContext> consumer) {
            var features = new WorldgenBuilder(selection);
            var effects  = new EffectsBuilder(selection);
            var climate  = new ClimateBuilder(selection);
            var environmentAttributes = new EnvironmentAttributesBuilder(selection);
            var spawns = new MobSpawnsBuilder(selection);

            BiomeModificationContext context = new BiomeModificationContext(features, effects, climate, environmentAttributes, spawns);
            consumer.accept(context);

            features.build();
            effects.build();
            climate.build();
            environmentAttributes.build();
            spawns.build();
        }

        @Override
        public void register(ResourceKey<Biome> biome, Consumer<BiomeModificationContext> consumer) {
            doRegister(selection -> selection.getBiomeHolder().is(biome), consumer);
        }

        @Override
        public void register(List<ResourceKey<Biome>> biomes, Consumer<BiomeModificationContext> consumer) {
            doRegister(selection -> biomes.contains(selection.getBiomeKey()), consumer);
        }

        @Override
        public void register(TagKey<Biome> tag, Consumer<BiomeModificationContext> consumer) {
            doRegister(selection -> selection.hasTag(tag), consumer);
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
}