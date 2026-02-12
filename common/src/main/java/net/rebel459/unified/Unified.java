package net.rebel459.unified;

import net.rebel459.unified.registry.UnifiedItemComponents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Unified {

    public static void initRegistries() {
        UnifiedItemComponents.init();
    }

    public static void init() {}

    public static final String MOD_ID = "unified";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

}
