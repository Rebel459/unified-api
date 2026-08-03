package net.rebel459.unified.fabric.client.core;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.rebel459.unified.api.client.core.UnifiedClientRegistries;

import java.util.function.Supplier;

public class FabricUnifiedClientRegistries {

    public record KeyMappings(String modId) implements UnifiedClientRegistries.KeyMappings {

        @Override
        public Supplier<KeyMapping> registerKeybind(String path, InputConstants.Type type, Integer key, KeyMapping.Category category) {
            var keyBind = Suppliers.memoize(() -> KeyMappingHelper.registerKeyMapping(
                    new KeyMapping(
                            "key." + modId + "." + path,
                            type,
                            key,
                            category
                    ))
            );
            keyBind.get();
            return keyBind;
        }

        @Override
        public KeyMapping.Category registerCategory(String path) {
            return KeyMapping.Category.register(Identifier.fromNamespaceAndPath(modId, path));
        }
    }
}