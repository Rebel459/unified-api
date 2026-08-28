package net.rebel459.unified.testmod;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.rebel459.unified.datagen.FabricUnifiedDatagen;

public final class UnifiedTestModDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricUnifiedDatagen.registerGenerator(generator);
    }
}
