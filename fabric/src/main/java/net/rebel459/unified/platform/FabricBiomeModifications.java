package net.rebel459.unified.platform;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;
import net.rebel459.unified.util.event.BiomeModificationContext;
import net.rebel459.unified.util.event.BiomeModificationsImpl;

import java.util.Optional;

public final class FabricBiomeModifications {
    private FabricBiomeModifications() {}

    public static void apply(MinecraftServer server) {
        HolderLookup.Provider provider = server.registryAccess();
        Registry<Biome> biomes = server.registryAccess().lookupOrThrow(Registries.BIOME);
        for (BiomeModificationsImpl.PreparedModification modification : BiomeModificationsImpl.prepare(provider)) {
            biomes.listElements().filter(modification.targets()).forEach(holder -> {
                Biome biome = holder.value();
                BiomeModificationContext editor = new BiomeModificationContext(provider, biome.climateSettings,
                        biome.generationSettings, biome.mobSettings, biome.attributes, biome.specialEffects);
                editor.apply(modification);
                if (!editor.changed()) return;
                biome.climateSettings = editor.climate();
                biome.generationSettings = editor.generation();
                biome.mobSettings = editor.mobSpawns();
                biome.attributes = editor.attributes();
                biome.specialEffects = editor.effects();
                markForNetworkSync(holder.key(), biomes);
            });
        }
    }

    @SuppressWarnings("unchecked")
    private static void markForNetworkSync(ResourceKey<Biome> key, Registry<Biome> registry) {
        if (registry instanceof MappedRegistry<?> mapped) {
            MappedRegistry<Biome> biomes = (MappedRegistry<Biome>) mapped;
            biomes.registrationInfos.computeIfPresent(key,
                    (ignored, info) -> new RegistrationInfo(Optional.empty(), info.lifecycle()));
        }
    }
}
