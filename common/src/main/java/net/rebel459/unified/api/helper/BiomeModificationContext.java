package net.rebel459.unified.api.helper;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public abstract class BiomeModificationContext {

    public abstract BiomeModificationContext.Worldgen getFeatures();
    public abstract BiomeModificationContext.Effects getEffects();
    public abstract BiomeModificationContext.Climate getClimate();
    public abstract Attributes getAttributes();

    public interface Worldgen {
        void addFeature(ResourceKey<PlacedFeature> feature, GenerationStep.Decoration step);
        void removeFeature(ResourceKey<PlacedFeature> feature, GenerationStep.Decoration step);
        void addCarver(ResourceKey<WorldCarver> carverKey);
        void removeCarver(ResourceKey<WorldCarver> carverKey);
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
    }
}