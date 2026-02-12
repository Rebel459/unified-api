package net.rebel459.unified.platform.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.rebel459.unified.platform.UnifiedFactory;
import org.lwjgl.glfw.GLFW;

import java.util.function.Supplier;

public class UnifiedClientRegistries {

    public interface KeyMappings {
        String modId();

        Supplier<KeyMapping> registerKeybind(String name, InputConstants.Type type, Integer key, KeyMapping.Category category);
        KeyMapping.Category registerCategory(String name);

        static KeyMappings create(String modId) {
            return UnifiedFactory.getClientRegistries().createKeyMappings(modId);
        }
    }
}