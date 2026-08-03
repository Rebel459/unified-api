package net.rebel459.unified;

import net.rebel459.unified.api.core.UnifiedPlatform;
import net.rebel459.unified.api.platform.ModLoader;
import net.rebel459.unified.api.registry.UnifiedDataComponents;
import net.rebel459.unified.api.builder.BlockSet;
import net.rebel459.unified.api.builder.EquipmentSet;
import net.rebel459.unified.api.builder.WoodSet;
import net.rebel459.unified.impl.builder.BlockSetImpl;
import net.rebel459.unified.impl.builder.EquipmentSetImpl;
import net.rebel459.unified.impl.builder.WoodSetImpl;
import net.rebel459.unified.impl.helper.StructureMusicImpl;

public class Unified {

    public static void initRegistries() {
        UnifiedDataComponents.init();
    }

    public static void init() {
        StructureMusicImpl.init();
        if (UnifiedPlatform.getModLoader() == ModLoader.NEOFORGE) {
            WoodSetImpl.init(WoodSet.WOOD_SETS);
            BlockSetImpl.init(BlockSet.BLOCK_SETS);
            EquipmentSetImpl.init(EquipmentSet.EQUIPMENT_SETS);
        }
    }

    public static final String MOD_ID = "unified";
}