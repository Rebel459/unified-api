package net.rebel459.unified.platform;

import net.minecraft.SharedConstants;
import net.rebel459.unified.util.LoaderType;
import net.rebel459.unified.util.VanillaVersion;
import org.jetbrains.annotations.ApiStatus;

public class UnifiedPlatform {

    @ApiStatus.Internal
    public static HelpersImpl.Platform get() {
        return InternalHandlerImpl.INSTANCE.getPlatform();
    }

    public static LoaderType getLoader() {
        return get().getLoader();
    }

    public static boolean isClientSide() {
        return get().isClientSide();
    }
    public static boolean isServerSide() {
        return get().isServerSide();
    }

    public static boolean isModLoaded(String modId) {
        return get().isModLoaded(modId);
    }

    public static boolean isDevelopmentEnvironment() {
        return get().isDevelopmentEnvironment();
    }
}
