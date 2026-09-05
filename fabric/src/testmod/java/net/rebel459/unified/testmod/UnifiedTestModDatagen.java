package net.rebel459.unified.testmod;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.rebel459.unified.fabric.FabricUnifiedDatagen;
import net.rebel459.unified.fabric.datagen.FabricDatagenProvider;

public final class UnifiedTestModDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricUnifiedDatagen.register(generator);
    }
}
