package net.rebel459.unified.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.rebel459.unified.Unified;
import net.rebel459.unified.mixin.BiomeAccessor;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class UnifiedBiomeModifiers {

    public static class ClimateType {
        public static String DOWNFALL = "downfall";
        public static String TEMPERATURE = "temperature";
    }

    public record SetClimateModifier(HolderSet<Biome> biomes, String type, float value) implements BiomeModifier {

        private static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Unified.MOD_ID);

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (this.biomes.contains(biome)) {
                if (Objects.equals(type, ClimateType.TEMPERATURE)) builder.getClimateSettings().setTemperature(this.value);
                else if (Objects.equals(type, ClimateType.DOWNFALL)) builder.getClimateSettings().setDownfall(this.value);
            }
        }

        @Override
        public @NonNull MapCodec<? extends BiomeModifier> codec() {
            return CODEC.get();
        }

        public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<UnifiedBiomeModifiers.SetClimateModifier>> CODEC = BIOME_MODIFIER_SERIALIZERS.register("set_climate", () -> RecordCodecBuilder.mapCodec(
                builder -> builder
                        .group(
                                Biome.LIST_CODEC.fieldOf("biomes").forGetter(UnifiedBiomeModifiers.SetClimateModifier::biomes),
                                Codec.STRING.fieldOf("climate_type").forGetter(UnifiedBiomeModifiers.SetClimateModifier::type),
                                Codec.FLOAT.fieldOf("step").forGetter(UnifiedBiomeModifiers.SetClimateModifier::value))
                        .apply(builder, UnifiedBiomeModifiers.SetClimateModifier::new)));
    }

    public record SetPrecipitationModifier(HolderSet<Biome> biomes, boolean hasPrecipitation) implements BiomeModifier {

        private static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Unified.MOD_ID);

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (this.biomes.contains(biome)) {
                builder.getClimateSettings().setHasPrecipitation(this.hasPrecipitation);
            }
        }

        @Override
        public @NonNull MapCodec<? extends BiomeModifier> codec() {
            return CODEC.get();
        }

        public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<UnifiedBiomeModifiers.SetPrecipitationModifier>> CODEC = BIOME_MODIFIER_SERIALIZERS.register("set_precipitation", () -> RecordCodecBuilder.mapCodec(
                builder -> builder
                        .group(
                                Biome.LIST_CODEC.fieldOf("biomes").forGetter(UnifiedBiomeModifiers.SetPrecipitationModifier::biomes),
                                Codec.BOOL.fieldOf("has_precipitation").forGetter(UnifiedBiomeModifiers.SetPrecipitationModifier::hasPrecipitation))
                        .apply(builder, UnifiedBiomeModifiers.SetPrecipitationModifier::new)));
    }

    public record SetEnvironmentAttributeModifier(HolderSet<Biome> biomes, EnvironmentAttribute attribute, Object value) implements BiomeModifier {

        private static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Unified.MOD_ID);

        public <Value> Pair<EnvironmentAttribute<Value>, Value> getPair() {
            return Pair.of((EnvironmentAttribute<Value>) this.attribute, (Value) this.value);
        }

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (this.biomes.contains(biome)) {
                var accessor = ((BiomeAccessor) (Object) biome.value());
                var attributes = EnvironmentAttributeMap.builder();
                attributes.putAll(accessor.getAttributes());
                attributes.set(getPair().getFirst(), getPair().getSecond());
                accessor.setAttributes(attributes.build());
            }
        }

        @Override
        public MapCodec<? extends BiomeModifier> codec() {
            return null;
        }
    }

    public static class EffectType {
        public static String WATER = "water";
        public static String FOLIAGE = "foliage";
        public static String DRY_FOLIAGE = "dry_foliage";
        public static String GRASS = "grass";
    }

    public record SetEffectModifier(HolderSet<Biome> biomes, String type, int color) implements BiomeModifier {

        private static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Unified.MOD_ID);

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (this.biomes.contains(biome)) {
                if (Objects.equals(type, EffectType.WATER)) builder.getSpecialEffects().waterColor(this.color);
                else if (Objects.equals(type, EffectType.FOLIAGE)) builder.getSpecialEffects().foliageColorOverride(this.color);
                else if (Objects.equals(type, EffectType.DRY_FOLIAGE)) builder.getSpecialEffects().dryFoliageColorOverride(this.color);
                else if (Objects.equals(type, EffectType.GRASS)) builder.getSpecialEffects().grassColorOverride(this.color);
            }
        }

        @Override
        public @NonNull MapCodec<? extends BiomeModifier> codec() {
            return CODEC.get();
        }

        public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<SetEffectModifier>> CODEC = BIOME_MODIFIER_SERIALIZERS.register("set_climate", () -> RecordCodecBuilder.mapCodec(
                builder -> builder
                        .group(
                                Biome.LIST_CODEC.fieldOf("biomes").forGetter(SetEffectModifier::biomes),
                                Codec.STRING.fieldOf("effect").forGetter(SetEffectModifier::type),
                                Codec.INT.fieldOf("color").forGetter(SetEffectModifier::color))
                        .apply(builder, SetEffectModifier::new)));
    }
}