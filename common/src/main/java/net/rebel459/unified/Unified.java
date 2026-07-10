package net.rebel459.unified;

import net.rebel459.unified.platform.UnifiedPlatform;
import net.rebel459.unified.registry.UnifiedDataComponents;
import net.rebel459.unified.util.LoaderType;
import net.rebel459.unified.util.builder.BlockSet;
import net.rebel459.unified.util.builder.EquipmentSet;
import net.rebel459.unified.util.builder.WoodSet;
import net.rebel459.unified.util.builder.impl.BlockSetImpl;
import net.rebel459.unified.util.builder.impl.EquipmentSetImpl;
import net.rebel459.unified.util.builder.impl.WoodSetImpl;
import net.rebel459.unified.util.helper.impl.StructureMusicImpl;

public class Unified {

    public static void initRegistries() {
        UnifiedDataComponents.init();
    }

    public static void init() {
        StructureMusicImpl.init();
        if (UnifiedPlatform.getLoader() == LoaderType.NEOFORGE) {
            WoodSetImpl.init(WoodSet.WOOD_SETS);
            BlockSetImpl.init(BlockSet.BLOCK_SETS);
            EquipmentSetImpl.init(EquipmentSet.EQUIPMENT_SETS);
        }
    }

    public static final String MOD_ID = "unified";
}