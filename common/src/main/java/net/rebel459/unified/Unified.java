package net.rebel459.unified;

import net.rebel459.unified.registry.UnifiedDataComponents;
import net.rebel459.unified.util.helper.StructureMusicImpl;

public class Unified {

    public static void initRegistries() {
        UnifiedDataComponents.init();
    }

    public static void init() {
        StructureMusicImpl.init();
    }

    public static final String MOD_ID = "unified";
}