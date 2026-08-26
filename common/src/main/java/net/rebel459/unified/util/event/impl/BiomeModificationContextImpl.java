package net.rebel459.unified.util.event.impl;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.rebel459.unified.util.event.BiomeModificationContext;
import net.rebel459.unified.util.data.BiomeModifiers;

import java.util.*;
import java.util.stream.Collectors;

public final class BiomeModificationContextImpl extends BiomeModificationContext {

    private final HolderLookup.Provider provider;

    private Biome.ClimateSettings climateSettings;
    private BiomeGenerationSettings generationSettings;
    private MobSpawnSettings mobSpawnSettings;
    private EnvironmentAttributeMap attributes;
    private BiomeSpecialEffects specialEffects;
    private boolean changed;

    private final Worldgen worldgen = new Worldgen() {
        @Override
        public void addFeature(ResourceKey<PlacedFeature> feature, GenerationStep.Decoration step) {
            List<List<Holder<PlacedFeature>>> features = copyFeatures();
            while (features.size() <= step.ordinal()) features.add(new ArrayList<>());
            features.get(step.ordinal()).add(provider.lookupOrThrow(Registries.PLACED_FEATURE).getOrThrow(feature));
            replaceGeneration(features, copyCarvers());
        }

        @Override
        public void removeFeature(ResourceKey<PlacedFeature> feature, GenerationStep.Decoration step) {
            List<List<Holder<PlacedFeature>>> features = copyFeatures();
            if (features.size() > step.ordinal()) features.get(step.ordinal()).removeIf(holder -> holder.is(feature));
            replaceGeneration(features, copyCarvers());
        }

        @Override
        public void addCarver(ResourceKey<ConfiguredWorldCarver<?>> carver) {
            List<Holder<ConfiguredWorldCarver<?>>> carvers = copyCarvers();
            carvers.add(provider.lookupOrThrow(Registries.CONFIGURED_CARVER).getOrThrow(carver));
            replaceGeneration(copyFeatures(), carvers);
        }

        @Override
        public void removeCarver(ResourceKey<ConfiguredWorldCarver<?>> carver) {
            List<Holder<ConfiguredWorldCarver<?>>> carvers = copyCarvers();
            carvers.removeIf(holder -> holder.is(carver));
            replaceGeneration(copyFeatures(), carvers);
        }
    };

    private final Effects effects = new Effects() {
        @Override
        public void setWaterColor(int color) {
            specialEffects(new BiomeSpecialEffects(color, specialEffects.foliageColorOverride(), specialEffects.dryFoliageColorOverride(), specialEffects.grassColorOverride(), specialEffects.grassColorModifier()));
        }

        @Override
        public void setFoliageColor(int color) {
            specialEffects(new BiomeSpecialEffects(specialEffects.waterColor(), Optional.of(color), specialEffects.dryFoliageColorOverride(), specialEffects.grassColorOverride(), specialEffects.grassColorModifier()));
        }

        @Override
        public void setDryFoliageColor(int color) {
            specialEffects(new BiomeSpecialEffects(specialEffects.waterColor(), specialEffects.foliageColorOverride(), Optional.of(color), specialEffects.grassColorOverride(), specialEffects.grassColorModifier()));
        }

        @Override
        public void setGrassColor(int color) {
            specialEffects(new BiomeSpecialEffects(specialEffects.waterColor(), specialEffects.foliageColorOverride(), specialEffects.dryFoliageColorOverride(), Optional.of(color), specialEffects.grassColorModifier()));
        }
    };

    private final Climate climate = new Climate() {
        @Override
        public void setTemperature(float temperature) {
            climate(new Biome.ClimateSettings(climateSettings.hasPrecipitation(), temperature, climateSettings.temperatureModifier(), climateSettings.downfall()));
        }

        @Override
        public void setDownfall(float downfall) {
            climate(new Biome.ClimateSettings(climateSettings.hasPrecipitation(), climateSettings.temperature(), climateSettings.temperatureModifier(), downfall));
        }

        @Override
        public void setPrecipitation(boolean precipitation) {
            climate(new Biome.ClimateSettings(precipitation, climateSettings.temperature(), climateSettings.temperatureModifier(), climateSettings.downfall()));
        }
    };

    private final EnvironmentAttributes environmentAttributes = new EnvironmentAttributes() {
        @Override
        public <Value> void set(EnvironmentAttribute<Value> attribute, Value value) {
            attributes(EnvironmentAttributeMap.builder().putAll(attributes).set(attribute, value).build());
        }
    };

    private final MobSpawns mobSpawns = new MobSpawns() {
        @Override
        public void addSpawn(MobSpawnSettings.SpawnerData data, int weight) {
            Map<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>> spawners = copySpawners();
            MobCategory category = data.type().getCategory();
            List<Weighted<MobSpawnSettings.SpawnerData>> entries = new ArrayList<>(spawners.getOrDefault(category, MobSpawnSettings.EMPTY_MOB_LIST).unwrap());
            entries.add(new Weighted<>(data, weight));
            spawners.put(category, WeightedList.of(entries));
            replaceMobSpawns(spawners, new HashMap<>(mobSpawnSettings.mobSpawnCosts));
        }

        @Override
        public void removeSpawn(EntityType<?> entityType) {
            Map<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>> spawners = copySpawners();

            for (MobCategory category : MobCategory.values()) {
                List<Weighted<MobSpawnSettings.SpawnerData>> entries = new ArrayList<>(spawners.getOrDefault(category, MobSpawnSettings.EMPTY_MOB_LIST).unwrap());
                if (entries.removeIf(entry -> entry.value().type() == entityType)) spawners.put(category, WeightedList.of(entries));
            }

            replaceMobSpawns(spawners, new HashMap<>(mobSpawnSettings.mobSpawnCosts));
        }

        @Override
        public void addCharge(EntityType<?> entityType, double charge, double energyBudget) {
            Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> costs = new HashMap<>(mobSpawnSettings.mobSpawnCosts);
            costs.put(entityType, new MobSpawnSettings.MobSpawnCost(energyBudget, charge));
            replaceMobSpawns(copySpawners(), costs);
        }

        @Override
        public void removeCharge(EntityType<?> entityType) {
            Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> costs = new HashMap<>(mobSpawnSettings.mobSpawnCosts);
            costs.remove(entityType);
            replaceMobSpawns(copySpawners(), costs);
        }
    };

    public BiomeModificationContextImpl(HolderLookup.Provider provider, Biome.ClimateSettings climateSettings, BiomeGenerationSettings generationSettings, MobSpawnSettings mobSpawnSettings, EnvironmentAttributeMap attributes, BiomeSpecialEffects specialEffects) {
        this.provider = provider;
        this.climateSettings = climateSettings;
        this.generationSettings = generationSettings;
        this.mobSpawnSettings = mobSpawnSettings;
        this.attributes = attributes;
        this.specialEffects = specialEffects;
    }

    @Override public Worldgen getFeatures() { return worldgen; }
    @Override public Effects getEffects() { return effects; }
    @Override public Climate getClimate() { return climate; }
    @Override public EnvironmentAttributes getEnvironmentAttributes() { return environmentAttributes; }
    @Override public MobSpawns getMobSpawns() { return mobSpawns; }

    public void apply(BiomeModifiers.PreparedModification modification, Holder.Reference<Biome> biome) {
        modification.modifier().modify(biome, this);
    }

    public void addAttributes(EnvironmentAttributeMap additions) {
        EnvironmentAttributeMap merged = EnvironmentAttributeMap.builder().putAll(attributes).putAll(additions).build();
        if (!merged.equals(attributes)) attributes(merged);
    }

    public boolean changed() { return changed; }
    public HolderLookup.Provider provider() { return provider; }
    public Biome.ClimateSettings climate() { return climateSettings; }
    public BiomeGenerationSettings generation() { return generationSettings; }
    public MobSpawnSettings mobSpawns() { return mobSpawnSettings; }
    public EnvironmentAttributeMap attributes() { return attributes; }
    public BiomeSpecialEffects effects() { return specialEffects; }
    public Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> spawnCosts() { return Map.copyOf(mobSpawnSettings.mobSpawnCosts); }

    private void climate(Biome.ClimateSettings climate) {
        climateSettings = climate;
        changed = true;
    }

    private void generation(BiomeGenerationSettings generation) {
        generationSettings = generation;
        changed = true;
    }

    private void mobSpawns(MobSpawnSettings mobSpawns) {
        mobSpawnSettings = mobSpawns;
        changed = true;
    }

    private void attributes(EnvironmentAttributeMap attributes) {
        this.attributes = attributes;
        changed = true;
    }

    private void specialEffects(BiomeSpecialEffects effects) {
        specialEffects = effects;
        changed = true;
    }

    private List<List<Holder<PlacedFeature>>> copyFeatures() {
        return generationSettings.features().stream()
                .map(set -> new ArrayList<>(set.stream().toList()))
                .map(list -> (List<Holder<PlacedFeature>>) list)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<Holder<ConfiguredWorldCarver<?>>> copyCarvers() {
        List<Holder<ConfiguredWorldCarver<?>>> carvers = new ArrayList<>();
        generationSettings.getCarvers().forEach(carvers::add);
        return carvers;
    }

    private void replaceGeneration(List<List<Holder<PlacedFeature>>> features, List<Holder<ConfiguredWorldCarver<?>>> carvers) {
        List<HolderSet<PlacedFeature>> featureSets = features.stream().map(values -> (HolderSet<PlacedFeature>) HolderSet.direct(values)).toList();
        generation(new BiomeGenerationSettings(HolderSet.direct(carvers), featureSets));
    }

    private Map<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>> copySpawners() {
        Map<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>> spawners = new EnumMap<>(MobCategory.class);
        spawners.putAll(mobSpawnSettings.spawners);
        return spawners;
    }

    private void replaceMobSpawns(Map<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>> spawners, Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> costs) {
        mobSpawns(new MobSpawnSettings(mobSpawnSettings.creatureGenerationProbability, spawners, costs));
    }
}