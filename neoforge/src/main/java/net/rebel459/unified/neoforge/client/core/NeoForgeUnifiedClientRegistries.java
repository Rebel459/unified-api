package net.rebel459.unified.neoforge.client.core;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.rebel459.unified.api.client.core.UnifiedClientRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class NeoForgeUnifiedClientRegistries {

    public record KeyMappings(String modId) implements UnifiedClientRegistries.KeyMappings {

        public static List<Supplier<KeyMapping>> MAPPINGS = new ArrayList<>();
        public static List<KeyMapping.Category> CATEGORIES = new ArrayList<>();

        @Override
        public Supplier<KeyMapping> registerKeybind(String path, InputConstants.Type type, Integer key, KeyMapping.Category category) {
            Supplier<KeyMapping> keyMapping = Suppliers.memoize(() -> new KeyMapping(
                    "key." + modId + "." + path,
                    type,
                    key,
                    category
            ));
            MAPPINGS.add(keyMapping);
            return keyMapping;
        }

        @Override
        public KeyMapping.Category registerCategory(String path) {
            KeyMapping.Category category = new KeyMapping.Category(Identifier.fromNamespaceAndPath(modId, path));
            CATEGORIES.add(category);
            return category;
        }

        @SubscribeEvent
        public static void registerBindings(RegisterKeyMappingsEvent event) {
            for (KeyMapping.Category category : CATEGORIES) {
                event.registerCategory(category);
            }

            for (Supplier<KeyMapping> keyMapping : MAPPINGS) {
                event.register(keyMapping.get());
            }
        }
    }
}