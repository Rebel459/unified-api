package net.rebel459.unified.api.data.helper;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.modifier.AttributeModifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.rebel459.unified.api.codec.CodecGenerator;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.impl.data.helper.BiomeModifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class BiomeModifierGenerator extends HelperGenerator {

    public BiomeModifierGenerator(String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
        super(modId, requirement);
    }
    
    public Builder create(String name, Function<HolderLookup.Provider, HolderSet<Biome>> targets) {
        return new Builder(name, targets, modId, requirement);
    }

    public static final class Builder extends HelperGenerator.Builder {
        private int priority = 0;

        private final List<BiomeModifiers.FeatureEntry> addFeatures = new ArrayList<>();
        private final List<BiomeModifiers.FeatureEntry> removeFeatures = new ArrayList<>();
        private final List<ResourceKey<ConfiguredWorldCarver<?>>> addCarvers = new ArrayList<>();
        private final List<ResourceKey<ConfiguredWorldCarver<?>>> removeCarvers = new ArrayList<>();

        private Optional<Integer> waterColor = Optional.empty();
        private Optional<Integer> foliageColor = Optional.empty();
        private Optional<Integer> dryFoliageColor = Optional.empty();
        private Optional<Integer> grassColor = Optional.empty();

        private Optional<Float> temperature = Optional.empty();
        private Optional<Float> downfall = Optional.empty();
        private Optional<Boolean> hasPrecipitation = Optional.empty();

        private final EnvironmentAttributeMap.Builder setAttributes = EnvironmentAttributeMap.builder();
        private final EnvironmentAttributeMap.Builder modifyAttributes = EnvironmentAttributeMap.builder();

        private final List<BiomeModifiers.SpawnEntry> addSpawns = new ArrayList<>();
        private final List<EntityType<?>> removeSpawns = new ArrayList<>();
        private final List<BiomeModifiers.ChargeEntry> addCharges = new ArrayList<>();
        private final List<EntityType<?>> removeCharges = new ArrayList<>();

        private Builder(String name, Function<HolderLookup.Provider, HolderSet<Biome>> targets, String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
            super(name, modId, requirement);
            CodecGenerator.data(modId, Identifier.fromNamespaceAndPath(modId, "unified/biome_modifiers/" + name), requirement, (provider, ops) ->
                    BiomeModifiers.Definition.CODEC.encodeStart(ops,
                            new BiomeModifiers.Definition(
                                    targets.apply(provider),
                                    priority,
                                    new BiomeModifiers.Worldgen(addFeatures, removeFeatures, addCarvers, removeCarvers),
                                    new BiomeModifiers.Effects(waterColor, foliageColor, dryFoliageColor, grassColor),
                                    new BiomeModifiers.Climate(temperature, downfall, hasPrecipitation),
                                    new BiomeModifiers.Attributes(setAttributes.build(), modifyAttributes.build()),
                                    new BiomeModifiers.Spawns(addSpawns, removeSpawns, addCharges, removeCharges)
                            )).getOrThrow()
            );
        }

        public Builder setPriority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder addFeature(ResourceKey<PlacedFeature> feature, GenerationStep.Decoration step) {
            addFeatures.add(new BiomeModifiers.FeatureEntry(feature, step));
            return this;
        }

        public Builder removeFeature(ResourceKey<PlacedFeature> feature, GenerationStep.Decoration step) {
            removeFeatures.add(new BiomeModifiers.FeatureEntry(feature, step));
            return this;
        }

        public Builder addCarver(ResourceKey<ConfiguredWorldCarver<?>> carver) {
            addCarvers.add(carver);
            return this;
        }

        public Builder removeCarver(ResourceKey<ConfiguredWorldCarver<?>> carver) {
            removeCarvers.add(carver);
            return this;
        }

        public Builder setWaterColor(int color) {
            waterColor = Optional.of(color);
            return this;
        }

        public Builder setFoliageColor(int color) {
            foliageColor = Optional.of(color);
            return this;
        }

        public Builder setDryFoliageColor(int color) {
            dryFoliageColor = Optional.of(color);
            return this;
        }

        public Builder setGrassColor(int color) {
            grassColor = Optional.of(color);
            return this;
        }

        public Builder setTemperature(float value) {
            temperature = Optional.of(value);
            return this;
        }

        public Builder setDownfall(float value) {
            downfall = Optional.of(value);
            return this;
        }

        public Builder setPrecipitation(boolean hasPrecipitation) {
            this.hasPrecipitation = Optional.of(hasPrecipitation);
            return this;
        }

        public <Value> Builder setAttribute(EnvironmentAttribute<Value> attribute, Value value) {
            setAttributes.set(attribute, value);
            return this;
        }

        public <Value, Parameter> Builder modifyAttribute(EnvironmentAttribute<Value> attribute, AttributeModifier<Value, Parameter> modifier, Parameter parameter) {
            modifyAttributes.modify(attribute, modifier, parameter);
            return this;
        }

        public Builder addSpawn(MobSpawnSettings.SpawnerData data, int weight) {
            addSpawns.add(new BiomeModifiers.SpawnEntry(data, weight));
            return this;
        }

        public Builder removeSpawn(EntityType<?> entity) {
            removeSpawns.add(entity);
            return this;
        }

        public Builder addCharge(EntityType<?> entity, double charge, double energyBudget) {
            addCharges.add(new BiomeModifiers.ChargeEntry(entity, charge, energyBudget));
            return this;
        }

        public Builder removeCharge(EntityType<?> entity) {
            removeCharges.add(entity);
            return this;
        }
    }
}
