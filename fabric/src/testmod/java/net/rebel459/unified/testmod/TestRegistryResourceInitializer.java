package net.rebel459.unified.testmod;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.rebel459.unified.api.core.RegistryResourceInitializer;
import net.rebel459.unified.api.core.RegistryResourceListener;
import net.rebel459.unified.impl.data.registry.ItemRegistry;

public final class TestRegistryResourceInitializer implements RegistryResourceInitializer {
    @Override
    public void initializeRegistryResources() {
        UnifiedTestMod.initRegistryResources();
        new RegistryResourceListener<>(
                Identifier.fromNamespaceAndPath(UnifiedTestMod.MOD_ID, "bootstrap_test"),
                Codec.STRING,
                ItemRegistry.ID
        ) {
            @Override
            protected void register(Identifier id, DeferredDeclaration<String> declaration) {
            }
        }.init();
    }
}
