package net.rebel459.unified.testmod;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.JukeboxSong;
import net.rebel459.unified.fabric.FabricUnifiedDatagen;

public final class UnifiedTestModDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricUnifiedDatagen.register(generator);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(Registries.JUKEBOX_SONG, context -> context.register(
                ResourceKey.create(Registries.JUKEBOX_SONG, Identifier.fromNamespaceAndPath(UnifiedTestMod.MOD_ID, "placeholder_song")),
                new JukeboxSong(SoundEvents.AMBIENT_CAVE, Component.literal("Placeholder Song"), 1.0F, 1)
        ));
    }
}
