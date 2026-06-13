package net.rebel459.unified;

import net.rebel459.unified.platform.UnifiedPlatform;
import net.rebel459.unified.registry.UnifiedDataComponents;
import net.rebel459.unified.util.LoaderType;
import net.rebel459.unified.util.helper.impl.StructureMusicImpl;
import net.rebel459.unified.util.registry.builder.Woodset;
import net.rebel459.unified.util.registry.builder.impl.WoodsetImpl;

public class Unified {

    public static void initRegistries() {
        UnifiedDataComponents.init();
    }

    public static void init() {
        StructureMusicImpl.init();
        if (UnifiedPlatform.getLoader() == LoaderType.NEOFORGE) WoodsetImpl.init(Woodset.WOODSETS);
    }

    public static final String MOD_ID = "unified";
}