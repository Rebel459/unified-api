package net.rebel459.unified.impl.data.helper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.codec.UnifiedCodecs;
import net.rebel459.unified.api.core.UnifiedHelpers;
import net.rebel459.unified.api.event.BiomeModificationContext;
import net.rebel459.unified.impl.event.BiomeModifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

public final class BiomeModifiers {

    public static final ResourceKey<Registry<Definition>> KEY = ResourceKey.createRegistryKey(Unified.id("biome_modifiers"));

    public static final List<EventEntry> EVENT_ENTRIES = new CopyOnWriteArrayList<>();

    private BiomeModifiers() {}

    public static void init() {
        UnifiedHelpers.DATA_REGISTRIES.register(KEY, UnifiedCodecs.loadRequirements(Definition.CODEC, () -> new Definition(
                HolderSet.direct(List.of()),
                0,
                Worldgen.EMPTY,
                Effects.EMPTY,
                Climate.EMPTY,
                Attributes.EMPTY,
                Spawns.EMPTY
        )));
    }

    public static List<PreparedModification> prepare(HolderLookup.Provider provider) {
        List<PreparedModification> entries = new ArrayList<>();
        int order = 0;

        for (EventEntry entry : EVENT_ENTRIES) {
            entries.add(new PreparedModification(entry.priority(), false, order++, _ -> true, entry.modifier()));
        }

        Optional<? extends HolderLookup.RegistryLookup<Definition>> definitions = provider.lookup(KEY);
        if (definitions.isPresent()) {
            for (Holder.Reference<Definition> holder : definitions.get().listElements().toList()) {
                Definition definition = holder.value();
                entries.add(new PreparedModification(definition.priority(), true, order++, definition.targets()::contains, (_, context) -> definition.apply((BiomeModifier) context)));
            }
        }

        entries.sort(Comparator.comparingInt(PreparedModification::priority).thenComparing(PreparedModification::datapack).thenComparingInt(PreparedModification::order));
        return List.copyOf(entries);
    }

    public record EventEntry(int priority, Entry modifier) {}
    public record PreparedModification(int priority, boolean datapack, int order, Predicate<Holder.Reference<Biome>> targets, Entry modifier) {}

    @FunctionalInterface
    public interface Entry {
        void modify(Holder.Reference<Biome> biome, BiomeModificationContext context);
    }

    public record Definition(
            HolderSet<Biome> targets,
            int priority,
            Worldgen worldgen,
            Effects effects,
            Climate climate,
            Attributes attributes,
            Spawns spawns
    ) {
        public static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Biome.LIST_CODEC.fieldOf("targets").forGetter(Definition::targets),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(Definition::priority),
                Worldgen.CODEC.optionalFieldOf("worldgen", Worldgen.EMPTY).forGetter(Definition::worldgen),
                Effects.CODEC.optionalFieldOf("effects", Effects.EMPTY).forGetter(Definition::effects),
                Climate.CODEC.optionalFieldOf("climate", Climate.EMPTY).forGetter(Definition::climate),
                Attributes.CODEC.optionalFieldOf("attributes", Attributes.EMPTY).forGetter(Definition::attributes),
                Spawns.CODEC.optionalFieldOf("spawns", Spawns.EMPTY).forGetter(Definition::spawns)
        ).apply(instance, Definition::new));

        private void apply(BiomeModifier context) {
            for (FeatureEntry entry : worldgen.addFeatures()) context.getFeatures().addFeature(entry.feature(), entry.step());
            for (FeatureEntry entry : worldgen.removeFeatures()) context.getFeatures().removeFeature(entry.feature(), entry.step());
            for (ResourceKey<ConfiguredWorldCarver<?>> carver : worldgen.addCarvers()) context.getFeatures().addCarver(carver);
            for (ResourceKey<ConfiguredWorldCarver<?>> carver : worldgen.removeCarvers()) context.getFeatures().removeCarver(carver);

            for (SpawnEntry entry : spawns.addSpawns()) context.getSpawns().addSpawn(entry.data(), entry.weight());
            for (EntityType<?> type : spawns.removeSpawns()) context.getSpawns().removeSpawn(type);
            for (ChargeEntry entry : spawns.addCharges()) context.getSpawns().addCharge(entry.type(), entry.charge(), entry.energyBudget());
            for (EntityType<?> type : spawns.removeCharges()) context.getSpawns().removeCharge(type);

            effects.waterColor().ifPresent(context.getEffects()::setWaterColor);
            effects.foliageColor().ifPresent(context.getEffects()::setFoliageColor);
            effects.dryFoliageColor().ifPresent(context.getEffects()::setDryFoliageColor);
            effects.grassColor().ifPresent(context.getEffects()::setGrassColor);

            climate.temperature().ifPresent(context.getClimate()::setTemperature);
            climate.downfall().ifPresent(context.getClimate()::setDownfall);
            climate.hasPrecipitation().ifPresent(context.getClimate()::setPrecipitation);

            attributes.apply(context);
        }
    }

    public record FeatureEntry(ResourceKey<PlacedFeature> feature, GenerationStep.Decoration step) {
        public static final Codec<FeatureEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(Registries.PLACED_FEATURE).fieldOf("feature").forGetter(FeatureEntry::feature),
                GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(FeatureEntry::step)
        ).apply(instance, FeatureEntry::new));
    }

    public record Worldgen(List<FeatureEntry> addFeatures, List<FeatureEntry> removeFeatures, List<ResourceKey<ConfiguredWorldCarver<?>>> addCarvers, List<ResourceKey<ConfiguredWorldCarver<?>>> removeCarvers) {
        public static final Worldgen EMPTY = new Worldgen(List.of(), List.of(), List.of(), List.of());
        private static final Codec<ResourceKey<ConfiguredWorldCarver<?>>> CONFIGURED_CARVER_CODEC = ResourceKey.codec(Registries.CONFIGURED_CARVER);

        public static final Codec<Worldgen> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                FeatureEntry.CODEC.listOf().optionalFieldOf("add_features", List.of()).forGetter(Worldgen::addFeatures),
                FeatureEntry.CODEC.listOf().optionalFieldOf("remove_features", List.of()).forGetter(Worldgen::removeFeatures),
                CONFIGURED_CARVER_CODEC.listOf().optionalFieldOf("add_carvers", List.of()).forGetter(Worldgen::addCarvers),
                CONFIGURED_CARVER_CODEC.listOf().optionalFieldOf("remove_carvers", List.of()).forGetter(Worldgen::removeCarvers)
        ).apply(instance, Worldgen::new));
    }

    public record Effects(Optional<Integer> waterColor, Optional<Integer> foliageColor, Optional<Integer> dryFoliageColor, Optional<Integer> grassColor) {
        public static final Effects EMPTY = new Effects(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

        public static final Codec<Effects> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.optionalFieldOf("water_color").forGetter(Effects::waterColor),
                Codec.INT.optionalFieldOf("foliage_color").forGetter(Effects::foliageColor),
                Codec.INT.optionalFieldOf("dry_foliage_color").forGetter(Effects::dryFoliageColor),
                Codec.INT.optionalFieldOf("grass_color").forGetter(Effects::grassColor)
        ).apply(instance, Effects::new));
    }

    public record Climate(Optional<Float> temperature, Optional<Float> downfall, Optional<Boolean> hasPrecipitation) {
        public static final Climate EMPTY = new Climate(Optional.empty(), Optional.empty(), Optional.empty());

        public static final Codec<Climate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.optionalFieldOf("temperature").forGetter(Climate::temperature),
                Codec.FLOAT.optionalFieldOf("downfall").forGetter(Climate::downfall),
                Codec.BOOL.optionalFieldOf("has_precipitation").forGetter(Climate::hasPrecipitation)
        ).apply(instance, Climate::new));
    }

    public record Attributes(EnvironmentAttributeMap set, EnvironmentAttributeMap modify) {
        public static final Attributes EMPTY = new Attributes(EnvironmentAttributeMap.EMPTY, EnvironmentAttributeMap.EMPTY);

        public static final Codec<Attributes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EnvironmentAttributeMap.CODEC.optionalFieldOf("set", EnvironmentAttributeMap.EMPTY).forGetter(Attributes::set),
                EnvironmentAttributeMap.CODEC.optionalFieldOf("modify", EnvironmentAttributeMap.EMPTY).forGetter(Attributes::modify)
        ).apply(instance, Attributes::new));

        private void apply(BiomeModifier context) {
            set.keySet().forEach((attribute) -> {
                set(context.getAttributes(), attribute, set);
            });
            modify.keySet().forEach((attribute) -> {
                modify(context.getAttributes(), attribute, modify);
            });
        }

        private <Value> void set(BiomeModificationContext.Attributes attributes, EnvironmentAttribute<Value> attribute, EnvironmentAttributeMap values) {
            attributes.set(attribute, values.applyModifier(attribute, attribute.defaultValue()));
        }

        private <Value> void modify(BiomeModificationContext.Attributes attributes, EnvironmentAttribute<Value> attribute, EnvironmentAttributeMap modifiers) {
            attributes.modify(attribute, current -> modifiers.applyModifier(attribute, current));
        }
    }

    public record SpawnEntry(MobSpawnSettings.SpawnerData data, int weight) {
        public static final Codec<SpawnEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MobSpawnSettings.SpawnerData.CODEC.forGetter(SpawnEntry::data),
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("weight").forGetter(SpawnEntry::weight)
        ).apply(instance, SpawnEntry::new));
    }

    public record ChargeEntry(EntityType<?> type, double charge, double energyBudget) {
        public static final Codec<ChargeEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter(ChargeEntry::type),
                Codec.DOUBLE.fieldOf("charge").forGetter(ChargeEntry::charge),
                Codec.DOUBLE.fieldOf("energy_budget").forGetter(ChargeEntry::energyBudget)
        ).apply(instance, ChargeEntry::new));
    }

    public record Spawns(List<SpawnEntry> addSpawns, List<EntityType<?>> removeSpawns, List<ChargeEntry> addCharges, List<EntityType<?>> removeCharges) {
        public static final Spawns EMPTY = new Spawns(List.of(), List.of(), List.of(), List.of());
        private static final Codec<EntityType<?>> ENTITY_TYPE_CODEC = BuiltInRegistries.ENTITY_TYPE.byNameCodec();

        public static final Codec<Spawns> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                SpawnEntry.CODEC.listOf().optionalFieldOf("add_spawns", List.of()).forGetter(Spawns::addSpawns),
                ENTITY_TYPE_CODEC.listOf().optionalFieldOf("remove_spawns", List.of()).forGetter(Spawns::removeSpawns),
                ChargeEntry.CODEC.listOf().optionalFieldOf("add_charges", List.of()).forGetter(Spawns::addCharges),
                ENTITY_TYPE_CODEC.listOf().optionalFieldOf("remove_charges", List.of()).forGetter(Spawns::removeCharges)
        ).apply(instance, Spawns::new));
    }
}
