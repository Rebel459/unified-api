package net.rebel459.unified.platform.client;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public class FabricUnifiedClientRegistries {

    public record KeyMappings(String modId) implements UnifiedClientRegistries.KeyMappings {

        @Override
        public Supplier<KeyMapping> registerKeybind(String name, InputConstants.Type type, Integer key, KeyMapping.Category category) {
            return Suppliers.memoize(() -> KeyBindingHelper.registerKeyBinding(
                    new KeyMapping(
                            "key." + modId + "." + name,
                            type,
                            key,
                            category
                    ))
            );
        }

        @Override
        public KeyMapping.Category registerCategory(String name) {
            return KeyMapping.Category.register(Identifier.fromNamespaceAndPath(modId, name));
        }
    }
}