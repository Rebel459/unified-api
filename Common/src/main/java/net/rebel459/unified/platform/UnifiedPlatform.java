package net.rebel459.unified.platform;

public interface UnifiedPlatform {

    UnifiedPlatform INSTANCE = UnifiedLoader.load(UnifiedPlatform.class);

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatform();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    String FABRIC = "fabric";
    String NEOFORGE = "neoforge";
}