package net.rebel459.unified.platform.client;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.rebel459.unified.platform.UnifiedFactory;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class NeoForgeUnifiedClientRegistries {

    public static void init() {
        UnifiedFactory.setClientRegistries(new UnifiedFactory.ClientRegistries() {
            @Override
            public UnifiedClientRegistries.KeyMappings createKeyMappings(String modId) {
                return new KeyMappings(modId);
            }
        });
    }

    public record KeyMappings(String modId) implements UnifiedClientRegistries.KeyMappings {

        public static List<Supplier<KeyMapping>> MAPPINGS = new ArrayList<>();
        public static List<KeyMapping.Category> CATEGORIES = new ArrayList<>();

        @Override
        public Supplier<KeyMapping> registerKeybind(String name, InputConstants.Type type, Integer key, KeyMapping.Category category) {
            Supplier<KeyMapping> keyMapping = Suppliers.memoize(() -> new KeyMapping(
                    "key." + modId + "." + name,
                    type,
                    key,
                    category
            ));
            MAPPINGS.add(keyMapping);
            return keyMapping;
        }

        @Override
        public KeyMapping.Category registerCategory(String name) {
            KeyMapping.Category category = new KeyMapping.Category(Identifier.fromNamespaceAndPath(modId, name));
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