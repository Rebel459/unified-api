package net.rebel459.unified.testmod;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.rebel459.unified.util.data.registry.ItemRegistry;
import net.rebel459.unified.util.registry.RegistryResourceInitializer;
import net.rebel459.unified.util.registry.RegistryResourceListener;

public final class TestRegistryResourceInitializer implements RegistryResourceInitializer {
    @Override
    public void initializeRegistryResources() {
        new RegistryResourceListener<String>(
                Identifier.fromNamespaceAndPath(UnifiedTestMod.MOD_ID, "bootstrap_test"),
                Codec.STRING,
                ItemRegistry.ID
        ) {
            @Override
            protected void register(Identifier id, String declaration) {
            }
        }.init();
    }
}
