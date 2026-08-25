package net.rebel459.unified;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.minecraft.resources.Identifier;
import net.rebel459.unified.platform.FabricUnifiedEvents;
import net.rebel459.unified.util.data.MobVariants;

public class UnifiedFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Unified.initRegistries();
        FabricUnifiedEvents.init();
        Unified.init();
    }
}
