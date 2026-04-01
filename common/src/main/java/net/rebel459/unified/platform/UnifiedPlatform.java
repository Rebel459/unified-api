package net.rebel459.unified.platform;

public class UnifiedPlatform {
    public static HelpersImpl.Platform get() {
        return InternalHandlerImpl.INSTANCE.getPlatform();
    }
}
