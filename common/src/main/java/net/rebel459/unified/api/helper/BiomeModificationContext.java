package net.rebel459.unified.api.helper;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.function.UnaryOperator;

public abstract class BiomeModificationContext {

    public abstract BiomeModificationContext.Worldgen getFeatures();
    public abstract BiomeModificationContext.Effects getEffects();
    public abstract BiomeModificationContext.Climate getClimate();
    public abstract Attributes getAttributes();
    public abstract Spawns getSpawns();

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

    public interface Attributes {
        <Value> void set(EnvironmentAttribute<Value> attribute, Value value);
        <Value> void modify(EnvironmentAttribute<Value> attribute, UnaryOperator<Value> modifier);
        void set(EnvironmentAttributeMap attributes);
        void modify(EnvironmentAttributeMap attributes);
    }

    public interface Spawns {
        void addSpawn(MobSpawnSettings.SpawnerData data, int weight);
        void removeSpawn(EntityType<?> entityType);
        void addCharge(EntityType<?> entityType, double charge, double energyBudget);
        void removeCharge(EntityType<?> entityType);
    }
}
