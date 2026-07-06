package net.rebel459.unified;

import net.rebel459.unified.platform.UnifiedPlatform;
import net.rebel459.unified.registry.UnifiedDataComponents;
import net.rebel459.unified.util.LoaderType;
import net.rebel459.unified.util.helper.impl.StructureMusicImpl;
import net.rebel459.unified.util.registry.builder.BlockSet;
import net.rebel459.unified.util.registry.builder.WoodSet;
import net.rebel459.unified.util.registry.builder.impl.BlockSetImpl;
import net.rebel459.unified.util.registry.builder.impl.WoodSetImpl;

public class Unified {

    public static void initRegistries() {
        UnifiedDataComponents.init();
    }

    public static void init() {
        StructureMusicImpl.init();
        if (UnifiedPlatform.getLoader() == LoaderType.NEOFORGE) {
            WoodSetImpl.init(WoodSet.WOODSETS);
            BlockSetImpl.init(BlockSet.BLOCKSETS);
        }
    }

    public static final String MOD_ID = "unified";
}