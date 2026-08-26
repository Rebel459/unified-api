package net.rebel459.unified.platform;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.rebel459.unified.util.event.impl.BiomeModificationContextImpl;
import net.rebel459.unified.util.data.BiomeModifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class NeoForgeBiomeModifications {
    private NeoForgeBiomeModifications() {}

    public static List<BiomeModifier> create(MinecraftServer server) {
        HolderLookup.Provider provider = server.registryAccess();
        Registry<Biome> biomes = server.registryAccess().lookupOrThrow(Registries.BIOME);
        List<BiomeModifiers.PreparedModification> modifications = BiomeModifiers.prepare(provider);
        markForNetworkSync(modifications, biomes);
        return modifications.stream().map(modification -> (BiomeModifier) new PreparedModifier(modification, provider)).toList();
    }

    @SuppressWarnings("unchecked")
    private static void markForNetworkSync(List<BiomeModifiers.PreparedModification> modifications,
                                           Registry<Biome> registry) {
        if (!(registry instanceof MappedRegistry<?> mapped)) return;
        MappedRegistry<Biome> biomes = (MappedRegistry<Biome>) mapped;
        biomes.listElements().filter(holder -> modifications.stream().anyMatch(entry -> entry.targets().test(holder)))
                .forEach(holder -> biomes.registrationInfos.computeIfPresent(holder.key(),
                        (ignored, info) -> new RegistrationInfo(Optional.empty(), info.lifecycle())));
    }

    private record PreparedModifier(BiomeModifiers.PreparedModification modification,
                                    HolderLookup.Provider provider) implements BiomeModifier {
        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase != Phase.AFTER_EVERYTHING || !(biome instanceof Holder.Reference<Biome> reference) || !modification.targets().test(reference)) return;

            ModifiableBiomeInfo.BiomeInfo current = builder.build();
            BiomeModificationContextImpl editor = new BiomeModificationContextImpl(provider, current.climateSettings(), current.generationSettings(), current.mobSpawnSettings(), biome.value().getAttributes(), current.effects());

            editor.apply(modification, reference);
            if (!editor.changed()) return;

            var climate = builder.getClimateSettings();
            climate.setHasPrecipitation(editor.climate().hasPrecipitation());
            climate.setTemperature(editor.climate().temperature());
            climate.setTemperatureModifier(editor.climate().temperatureModifier());
            climate.setDownfall(editor.climate().downfall());

            var effects = builder.getSpecialEffects();
            effects.waterColor(editor.effects().waterColor());
            editor.effects().foliageColorOverride().ifPresent(effects::foliageColorOverride);
            editor.effects().dryFoliageColorOverride().ifPresent(effects::dryFoliageColorOverride);
            editor.effects().grassColorOverride().ifPresent(effects::grassColorOverride);
            effects.grassColorModifier(editor.effects().grassColorModifier());

            var generation = builder.getGenerationSettings();
            for (GenerationStep.Decoration step : GenerationStep.Decoration.values()) {
                List<Holder<PlacedFeature>> features = generation.getFeatures(step);
                features.clear();
                if (editor.generation().features().size() > step.ordinal()) features.addAll(editor.generation().features().get(step.ordinal()).stream().toList());
            }

            generation.getCarvers().clear();
            editor.generation().getCarvers().forEach(generation.getCarvers()::add);

            var spawns = builder.getMobSpawnSettings();
            for (MobCategory category : MobCategory.values()) {
                var target = spawns.getSpawner(category);
                target.removeIf(ignored -> true);
                for (var entry : editor.mobSpawns().getMobs(category).unwrap()) target.add(entry.value(), entry.weight());
            }

            List<EntityType<?>> existingCosts = new ArrayList<>(spawns.getEntityTypes());
            spawns.removeSpawnCost(existingCosts.toArray(EntityType[]::new));
            editor.spawnCosts().forEach((type, cost) -> spawns.addMobCharge(type, cost.charge(), cost.energyBudget()));
            spawns.creatureGenerationProbability(editor.mobSpawns().getCreatureProbability());

            biome.value().attributes = editor.attributes();
        }

        @Override
        public MapCodec<? extends BiomeModifier> codec() {
            return MapCodec.unit(this);
        }
    }
}
