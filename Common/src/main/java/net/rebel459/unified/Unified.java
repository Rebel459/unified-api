package net.rebel459.unified;

import net.rebel459.unified.registry.UnifiedComponents;
import net.rebel459.unified.test.UnifiedTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Unified {

    public static void init() {
        UnifiedTest.afterInit();
    }

    public static void initRegistries() {
        UnifiedComponents.init();
        UnifiedTest.init();
    }

    public static final String MOD_ID = "unified";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

}
