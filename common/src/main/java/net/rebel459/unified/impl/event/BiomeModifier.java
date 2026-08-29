package net.rebel459.unified.impl.event;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.rebel459.unified.api.helper.BiomeModificationContext;
import net.rebel459.unified.impl.data.BiomeModifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class BiomeModifier extends BiomeModificationContext {

    private final HolderLookup.Provider provider;

    private Biome.ClimateSettings climateSettings;
    private BiomeGenerationSettings generationSettings;
    private EnvironmentAttributeMap attributeMap;
    private BiomeSpecialEffects specialEffects;
    private boolean changed;

    private final BiomeModificationContext.Worldgen worldgen = new Worldgen() {
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
        public void addCarver(ResourceKey<WorldCarver> carver) {
            List<Holder<WorldCarver>> carvers = copyCarvers();
            carvers.add(provider.lookupOrThrow(Registries.CARVER).getOrThrow(carver));
            replaceGeneration(copyFeatures(), carvers);
        }

        @Override
        public void removeCarver(ResourceKey<WorldCarver> carver) {
            List<Holder<WorldCarver>> carvers = copyCarvers();
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

    private final Attributes attributes = new Attributes() {
        @Override
        public <Value> void set(EnvironmentAttribute<Value> attribute, Value value) {
            attributes(EnvironmentAttributeMap.builder().putAll(attributeMap).set(attribute, value).build());
        }
    };

    public BiomeModifier(HolderLookup.Provider provider, Biome.ClimateSettings climateSettings, BiomeGenerationSettings generationSettings, EnvironmentAttributeMap attributeMap, BiomeSpecialEffects specialEffects) {
        this.provider = provider;
        this.climateSettings = climateSettings;
        this.generationSettings = generationSettings;
        this.attributeMap = attributeMap;
        this.specialEffects = specialEffects;
    }

    @Override public Worldgen getFeatures() { return worldgen; }
    @Override public Effects getEffects() { return effects; }
    @Override public Climate getClimate() { return climate; }
    @Override public Attributes getAttributes() { return attributes; }

    public void apply(BiomeModifiers.PreparedModification modification, Holder.Reference<Biome> biome) {
        modification.modifier().modify(biome, this);
    }

    public void addAttributes(EnvironmentAttributeMap additions) {
        EnvironmentAttributeMap merged = EnvironmentAttributeMap.builder().putAll(attributeMap).putAll(additions).build();
        if (!merged.equals(attributeMap)) attributes(merged);
    }

    public boolean changed() { return changed; }
    public HolderLookup.Provider provider() { return provider; }
    public Biome.ClimateSettings climate() { return climateSettings; }
    public BiomeGenerationSettings generation() { return generationSettings; }
    public EnvironmentAttributeMap attributes() { return attributeMap; }
    public BiomeSpecialEffects effects() { return specialEffects; }

    private void climate(Biome.ClimateSettings climate) {
        climateSettings = climate;
        changed = true;
    }

    private void generation(BiomeGenerationSettings generation) {
        generationSettings = generation;
        changed = true;
    }

    private void attributes(EnvironmentAttributeMap attributeMap) {
        this.attributeMap = attributeMap;
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

    private List<Holder<WorldCarver>> copyCarvers() {
        List<Holder<WorldCarver>> carvers = new ArrayList<>();
        generationSettings.getCarvers().forEach(carvers::add);
        return carvers;
    }

    private void replaceGeneration(List<List<Holder<PlacedFeature>>> features, List<Holder<WorldCarver>> carvers) {
        List<HolderSet<PlacedFeature>> featureSets = features.stream().map(values -> (HolderSet<PlacedFeature>) HolderSet.direct(values)).toList();
        generation(new BiomeGenerationSettings(HolderSet.direct(carvers), featureSets));
    }
}