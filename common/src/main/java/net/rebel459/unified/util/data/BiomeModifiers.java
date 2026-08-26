package net.rebel459.unified.util.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.rebel459.unified.Unified;
import net.rebel459.unified.platform.UnifiedHelpers;
import net.rebel459.unified.util.event.BiomeModificationContext;
import net.rebel459.unified.util.event.impl.BiomeModificationContextImpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class BiomeModifiers {

    public static final ResourceKey<Registry<Definition>> KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "biome_modifiers"));

    public static final List<EventEntry> EVENT_ENTRIES = new CopyOnWriteArrayList<>();

    private BiomeModifiers() {}

    public static void init() {
        UnifiedHelpers.DATA_REGISTRIES.register(KEY, Definition.CODEC);
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
                entries.add(new PreparedModification(definition.priority(), true, order++, definition.targets()::contains, (_, context) -> definition.apply((BiomeModificationContextImpl) context)));
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
            EnvironmentAttributeMap attributes,
            MobSpawns mobSpawns
    ) {
        public static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Biome.LIST_CODEC.fieldOf("targets").forGetter(Definition::targets),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(Definition::priority),
                Worldgen.CODEC.optionalFieldOf("worldgen", Worldgen.EMPTY).forGetter(Definition::worldgen),
                Effects.CODEC.optionalFieldOf("effects", Effects.EMPTY).forGetter(Definition::effects),
                Climate.CODEC.optionalFieldOf("climate", Climate.EMPTY).forGetter(Definition::climate),
                EnvironmentAttributeMap.CODEC.optionalFieldOf("attributes", EnvironmentAttributeMap.EMPTY).forGetter(Definition::attributes),
                MobSpawns.CODEC.optionalFieldOf("mob_spawns", MobSpawns.EMPTY).forGetter(Definition::mobSpawns)
        ).apply(instance, Definition::new));

        private void apply(BiomeModificationContextImpl context) {
            for (FeatureEntry entry : worldgen.addFeatures()) context.getFeatures().addFeature(entry.feature(), entry.step());
            for (FeatureEntry entry : worldgen.removeFeatures()) context.getFeatures().removeFeature(entry.feature(), entry.step());
            for (ResourceKey<ConfiguredWorldCarver<?>> carver : worldgen.addCarvers()) context.getFeatures().addCarver(carver);
            for (ResourceKey<ConfiguredWorldCarver<?>> carver : worldgen.removeCarvers()) context.getFeatures().removeCarver(carver);

            effects.waterColor().ifPresent(context.getEffects()::setWaterColor);
            effects.foliageColor().ifPresent(context.getEffects()::setFoliageColor);
            effects.dryFoliageColor().ifPresent(context.getEffects()::setDryFoliageColor);
            effects.grassColor().ifPresent(context.getEffects()::setGrassColor);

            climate.temperature().ifPresent(context.getClimate()::setTemperature);
            climate.downfall().ifPresent(context.getClimate()::setDownfall);
            climate.hasPrecipitation().ifPresent(context.getClimate()::setPrecipitation);

            context.addAttributes(attributes);

            for (SpawnEntry entry : mobSpawns.addSpawns()) context.getMobSpawns().addSpawn(entry.data(), entry.weight());
            for (EntityType<?> type : mobSpawns.removeSpawns()) context.getMobSpawns().removeSpawn(type);
            for (ChargeEntry entry : mobSpawns.addCharges()) context.getMobSpawns().addCharge(entry.type(), entry.charge(), entry.energyBudget());
            for (EntityType<?> type : mobSpawns.removeCharges()) context.getMobSpawns().removeCharge(type);
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
        private static final Codec<ResourceKey<ConfiguredWorldCarver<?>>> CARVER_CODEC = ResourceKey.codec(Registries.CONFIGURED_CARVER);

        public static final Codec<Worldgen> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                FeatureEntry.CODEC.listOf().optionalFieldOf("add_features", List.of()).forGetter(Worldgen::addFeatures),
                FeatureEntry.CODEC.listOf().optionalFieldOf("remove_features", List.of()).forGetter(Worldgen::removeFeatures),
                CARVER_CODEC.listOf().optionalFieldOf("add_carvers", List.of()).forGetter(Worldgen::addCarvers),
                CARVER_CODEC.listOf().optionalFieldOf("remove_carvers", List.of()).forGetter(Worldgen::removeCarvers)
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

    public record MobSpawns(List<SpawnEntry> addSpawns, List<EntityType<?>> removeSpawns, List<ChargeEntry> addCharges, List<EntityType<?>> removeCharges) {
        public static final MobSpawns EMPTY = new MobSpawns(List.of(), List.of(), List.of(), List.of());
        private static final Codec<EntityType<?>> ENTITY_TYPE_CODEC = BuiltInRegistries.ENTITY_TYPE.byNameCodec();

        public static final Codec<MobSpawns> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                SpawnEntry.CODEC.listOf().optionalFieldOf("add_spawns", List.of()).forGetter(MobSpawns::addSpawns),
                ENTITY_TYPE_CODEC.listOf().optionalFieldOf("remove_spawns", List.of()).forGetter(MobSpawns::removeSpawns),
                ChargeEntry.CODEC.listOf().optionalFieldOf("add_charges", List.of()).forGetter(MobSpawns::addCharges),
                ENTITY_TYPE_CODEC.listOf().optionalFieldOf("remove_charges", List.of()).forGetter(MobSpawns::removeCharges)
        ).apply(instance, MobSpawns::new));
    }
}