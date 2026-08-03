package net.rebel459.unified.api.helper;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class BiomeModificationContext {
    private final Worldgen worldgen;
    private final Effects effects;
    private final Climate climate;
    private final EnvironmentAttributes environmentAttributes;
    private final MobSpawns mobSpawns;

    public BiomeModificationContext(Worldgen worldgen, Effects effects, Climate climate, EnvironmentAttributes environmentAttributes, MobSpawns mobSpawns) {
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

    public interface Worldgen {
        void addFeature(ResourceKey<PlacedFeature> feature, GenerationStep.Decoration step);
        void removeFeature(ResourceKey<PlacedFeature> feature, GenerationStep.Decoration step);
        void addCarver(ResourceKey<ConfiguredWorldCarver<?>> carverKey);
        void removeCarver(ResourceKey<ConfiguredWorldCarver<?>> carverKey);
    }

    public interface Effects {
        void setWaterColor(int color);
        void setFoliageColor(int color);
        void setDryFoliageColor(int color);
        void setGrassColor(int color);
    }

    public interface Climate {
        void setTemperature(float temperature);
        void setDownfall(float downfall);
        void setPrecipitation(boolean hasPrecipitation);
    }

    public interface EnvironmentAttributes {
        <Value> void set(EnvironmentAttribute<Value> attribute, Value value);
    }

    public interface MobSpawns {
        void addSpawn(MobSpawnSettings.SpawnerData data, int weight);
        void removeSpawn(EntityType<?> entityType);

        void addCharge(EntityType<?> entityType, double charge, double energyBudget);
        void removeCharge(EntityType<?> entityType);
    }
}