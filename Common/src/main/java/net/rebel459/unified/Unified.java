package net.rebel459.unified;

import net.rebel459.unified.test.UnifiedTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ServiceLoader;

public class Unified {

    public static void initialize() {
        UnifiedTest.init();
    }

    public static final String MOD_ID = "unified";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

}
