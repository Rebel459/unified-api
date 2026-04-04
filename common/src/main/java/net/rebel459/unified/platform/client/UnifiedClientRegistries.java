package net.rebel459.unified.platform.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

import java.util.function.Supplier;

public class UnifiedClientRegistries {

    public interface KeyMappings {
        String modId();

        Supplier<KeyMapping> registerKeybind(String path, InputConstants.Type type, Integer key, KeyMapping.Category category);
        KeyMapping.Category registerCategory(String path);

        static UnifiedClientRegistries.KeyMappings create(String modId) {
            return ClientInternalHandlerImpl.INSTANCE.createKeyMappings(modId);
        }
    }
}