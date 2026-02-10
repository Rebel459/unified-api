package net.rebel459.unified;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.FuelRegistryEvents;
import net.minecraft.world.level.block.entity.FuelValues;
import net.rebel459.unified.platform.FabricUnifiedRegistries;
import net.rebel459.unified.platform.RegistryFactory;
import net.rebel459.unified.platform.UnifiedRegistries;
import net.rebel459.unified.test.UnifiedTest;

public class UnifiedFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        FabricUnifiedRegistries.init();
        UnifiedTest.TEST_ITEM.get();
        UnifiedTest.TEST_BLOCK.get();
        Unified.initialize();
    }
}
