package net.rebel459.unified.util.event;

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

import java.util.*;

public class BiomeModificationContext {

    private static final ThreadLocal<BiomeModificationContext> CURRENT = new ThreadLocal<>();
    private static final Worldgen WORLDGEN = new Worldgen() {};
    private static final Effects EFFECTS = new Effects() {};
    private static final Climate CLIMATE = new Climate() {};
    private static final EnvironmentAttributes ENVIRONMENT_ATTRIBUTES = new EnvironmentAttributes() {};
    private static final MobSpawns MOB_SPAWNS = new MobSpawns() {};

    private HolderLookup.Provider provider;
    private Biome.ClimateSettings climateSettings;
    private BiomeGenerationSettings generationSettings;
    private MobSpawnSettings mobSpawnSettings;
    private EnvironmentAttributeMap attributes;
    private BiomeSpecialEffects specialEffects;
    private boolean changed;

    protected BiomeModificationContext() {}

    public BiomeModificationContext(HolderLookup.Provider provider, Biome.ClimateSettings climateSettings, BiomeGenerationSettings generationSettings, MobSpawnSettings mobSpawnSettings, EnvironmentAttributeMap attributes, BiomeSpecialEffects specialEffects) {
        this.provider = provider;
        this.climateSettings = climateSettings;
        this.generationSettings = generationSettings;
        this.mobSpawnSettings = mobSpawnSettings;
        this.attributes = attributes;
        this.specialEffects = specialEffects;
    }

    public Worldgen getFeatures() {
        return WORLDGEN;
    }

    public Effects getEffects() {
        return EFFECTS;
    }

    public Climate getClimate() {
        return CLIMATE;
    }

    public EnvironmentAttributes getEnvironmentAttributes() {
        return ENVIRONMENT_ATTRIBUTES;
    }

    public MobSpawns getMobSpawns() {
        return MOB_SPAWNS;
    }

    public void apply(BiomeModificationsImpl.PreparedModification modification) {
        BiomeModificationContext previous = CURRENT.get();
        CURRENT.set(this);
        try {
            modification.modifier().accept(this);
        } finally {
            if (previous == null) CURRENT.remove();
            else CURRENT.set(previous);
        }
        if (!modification.attributes().equals(EnvironmentAttributeMap.EMPTY)) {
            attributes(EnvironmentAttributeMap.builder().putAll(attributes).putAll(modification.attributes()).build());
        }
    }

    public boolean changed() { return changed; }
    public HolderLookup.Provider provider() { return provider; }
    public Biome.ClimateSettings climate() { return climateSettings; }
    public BiomeGenerationSettings generation() { return generationSettings; }
    public MobSpawnSettings mobSpawns() { return mobSpawnSettings; }
    public EnvironmentAttributeMap attributes() { return attributes; }
    public BiomeSpecialEffects effects() { return specialEffects; }
    public Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> spawnCosts() { return Map.copyOf(mobSpawnSettings.mobSpawnCosts); }

    public void climate(Biome.ClimateSettings climate) { this.climateSettings = climate; changed = true; }
    public void generation(BiomeGenerationSettings generation) { this.generationSettings = generation; changed = true; }
    public void mobSpawns(MobSpawnSettings mobSpawns) { this.mobSpawnSettings = mobSpawns; changed = true; }
    public void attributes(EnvironmentAttributeMap attributes) { this.attributes = attributes; changed = true; }
    public void effects(BiomeSpecialEffects effects) { this.specialEffects = effects; changed = true; }

    public static BiomeModificationContext current() {
        BiomeModificationContext context = CURRENT.get();
        if (context == null) throw new IllegalStateException("Biome modification context used outside its callback");
        return context;
    }

    public interface Worldgen {
        default void addFeature(ResourceKey<PlacedFeature> feature, GenerationStep.Decoration step) {
            BiomeModificationContext editor = BiomeModificationContext.current();
            List<List<Holder<PlacedFeature>>> features = copyFeatures(editor);
            while (features.size() <= step.ordinal()) features.add(new ArrayList<>());
            features.get(step.ordinal()).add(editor.provider().lookupOrThrow(Registries.PLACED_FEATURE).getOrThrow(feature));
            replaceGeneration(editor, features, copyCarvers(editor));
        }

        default void removeFeature(ResourceKey<PlacedFeature> feature, GenerationStep.Decoration step) {
            BiomeModificationContext editor = BiomeModificationContext.current();
            List<List<Holder<PlacedFeature>>> features = copyFeatures(editor);
            if (features.size() > step.ordinal()) features.get(step.ordinal()).removeIf(holder -> holder.is(feature));
            replaceGeneration(editor, features, copyCarvers(editor));
        }

        default void addCarver(ResourceKey<ConfiguredWorldCarver<?>> carverKey) {
            BiomeModificationContext editor = BiomeModificationContext.current();
            List<Holder<ConfiguredWorldCarver<?>>> carvers = copyCarvers(editor);
            carvers.add(editor.provider().lookupOrThrow(Registries.CONFIGURED_CARVER).getOrThrow(carverKey));
            replaceGeneration(editor, copyFeatures(editor), carvers);
        }

        default void removeCarver(ResourceKey<ConfiguredWorldCarver<?>> carverKey) {
            BiomeModificationContext editor = BiomeModificationContext.current();
            List<Holder<ConfiguredWorldCarver<?>>> carvers = copyCarvers(editor);
            carvers.removeIf(holder -> holder.is(carverKey));
            replaceGeneration(editor, copyFeatures(editor), carvers);
        }

        private static List<List<Holder<PlacedFeature>>> copyFeatures(BiomeModificationContext editor) {
            return editor.generation().features().stream()
                    .map(set -> new ArrayList<>(set.stream().toList()))
                    .map(list -> (List<Holder<PlacedFeature>>) list)
                    .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        }

        private static List<Holder<ConfiguredWorldCarver<?>>> copyCarvers(BiomeModificationContext editor) {
            List<Holder<ConfiguredWorldCarver<?>>> carvers = new ArrayList<>();
            editor.generation().getCarvers().forEach(carvers::add);
            return carvers;
        }

        private static void replaceGeneration(BiomeModificationContext editor,
                                              List<List<Holder<PlacedFeature>>> features,
                                              List<Holder<ConfiguredWorldCarver<?>>> carvers) {
            List<HolderSet<PlacedFeature>> featureSets = features.stream()
                    .map(values -> (HolderSet<PlacedFeature>) HolderSet.direct(values)).toList();
            editor.generation(new BiomeGenerationSettings(HolderSet.direct(carvers), featureSets));
        }
    }

    public interface Effects {
        default void setWaterColor(int color) {
            BiomeModificationContext editor = BiomeModificationContext.current();
            BiomeSpecialEffects effects = editor.effects();
            editor.effects(new BiomeSpecialEffects(color, effects.foliageColorOverride(),
                    effects.dryFoliageColorOverride(), effects.grassColorOverride(), effects.grassColorModifier()));
        }

        default void setFoliageColor(int color) {
            BiomeModificationContext editor = BiomeModificationContext.current();
            BiomeSpecialEffects effects = editor.effects();
            editor.effects(new BiomeSpecialEffects(effects.waterColor(), Optional.of(color),
                    effects.dryFoliageColorOverride(), effects.grassColorOverride(), effects.grassColorModifier()));
        }

        default void setDryFoliageColor(int color) {
            BiomeModificationContext editor = BiomeModificationContext.current();
            BiomeSpecialEffects effects = editor.effects();
            editor.effects(new BiomeSpecialEffects(effects.waterColor(), effects.foliageColorOverride(),
                    Optional.of(color), effects.grassColorOverride(), effects.grassColorModifier()));
        }

        default void setGrassColor(int color) {
            BiomeModificationContext editor = BiomeModificationContext.current();
            BiomeSpecialEffects effects = editor.effects();
            editor.effects(new BiomeSpecialEffects(effects.waterColor(), effects.foliageColorOverride(),
                    effects.dryFoliageColorOverride(), Optional.of(color), effects.grassColorModifier()));
        }
    }

    public interface Climate {
        default void setTemperature(float temperature) {
            BiomeModificationContext editor = BiomeModificationContext.current();
            Biome.ClimateSettings climate = editor.climate();
            editor.climate(new Biome.ClimateSettings(climate.hasPrecipitation(), temperature,
                    climate.temperatureModifier(), climate.downfall()));
        }

        default void setDownfall(float downfall) {
            BiomeModificationContext editor = BiomeModificationContext.current();
            Biome.ClimateSettings climate = editor.climate();
            editor.climate(new Biome.ClimateSettings(climate.hasPrecipitation(), climate.temperature(),
                    climate.temperatureModifier(), downfall));
        }

        default void setPrecipitation(boolean hasPrecipitation) {
            BiomeModificationContext editor = BiomeModificationContext.current();
            Biome.ClimateSettings climate = editor.climate();
            editor.climate(new Biome.ClimateSettings(hasPrecipitation, climate.temperature(),
                    climate.temperatureModifier(), climate.downfall()));
        }
    }

    public interface EnvironmentAttributes {
        default <Value> void set(EnvironmentAttribute<Value> attribute, Value value) {
            BiomeModificationContext editor = BiomeModificationContext.current();
            editor.attributes(EnvironmentAttributeMap.builder().putAll(editor.attributes()).set(attribute, value).build());
        }
    }

    public interface MobSpawns {
        default void addSpawn(MobSpawnSettings.SpawnerData data, int weight) {
            BiomeModificationContext editor = BiomeModificationContext.current();
            Map<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>> spawners = copySpawners(editor);
            MobCategory category = data.type().getCategory();
            List<Weighted<MobSpawnSettings.SpawnerData>> entries = new ArrayList<>(
                    spawners.getOrDefault(category, MobSpawnSettings.EMPTY_MOB_LIST).unwrap());
            entries.add(new Weighted<>(data, weight));
            spawners.put(category, WeightedList.of(entries));
            replaceMobSpawns(editor, spawners, new HashMap<>(editor.mobSpawns().mobSpawnCosts));
        }

        default void removeSpawn(EntityType<?> entityType) {
            BiomeModificationContext editor = BiomeModificationContext.current();
            Map<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>> spawners = copySpawners(editor);
            for (MobCategory category : MobCategory.values()) {
                List<Weighted<MobSpawnSettings.SpawnerData>> entries = new ArrayList<>(
                        spawners.getOrDefault(category, MobSpawnSettings.EMPTY_MOB_LIST).unwrap());
                if (entries.removeIf(entry -> entry.value().type() == entityType)) {
                    spawners.put(category, WeightedList.of(entries));
                }
            }
            replaceMobSpawns(editor, spawners, new HashMap<>(editor.mobSpawns().mobSpawnCosts));
        }

        default void addCharge(EntityType<?> entityType, double charge, double energyBudget) {
            BiomeModificationContext editor = BiomeModificationContext.current();
            Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> costs = new HashMap<>(editor.mobSpawns().mobSpawnCosts);
            costs.put(entityType, new MobSpawnSettings.MobSpawnCost(energyBudget, charge));
            replaceMobSpawns(editor, copySpawners(editor), costs);
        }

        default void removeCharge(EntityType<?> entityType) {
            BiomeModificationContext editor = BiomeModificationContext.current();
            Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> costs = new HashMap<>(editor.mobSpawns().mobSpawnCosts);
            costs.remove(entityType);
            replaceMobSpawns(editor, copySpawners(editor), costs);
        }

        private static Map<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>> copySpawners(
                BiomeModificationContext editor) {
            Map<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>> spawners = new EnumMap<>(MobCategory.class);
            spawners.putAll(editor.mobSpawns().spawners);
            return spawners;
        }

        private static void replaceMobSpawns(BiomeModificationContext editor,
                                             Map<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>> spawners,
                                             Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> costs) {
            editor.mobSpawns(new MobSpawnSettings(editor.mobSpawns().creatureGenerationProbability, spawners, costs));
        }
    }
}
