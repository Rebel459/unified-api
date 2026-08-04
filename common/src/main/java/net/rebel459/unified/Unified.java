package net.rebel459.unified;

import net.rebel459.unified.api.core.UnifiedInstance;
import net.rebel459.unified.api.platform.ModLoader;
import net.rebel459.unified.api.registry.UnifiedDataComponents;
import net.rebel459.unified.api.builder.BlockSet;
import net.rebel459.unified.api.builder.EquipmentSet;
import net.rebel459.unified.api.builder.WoodSet;
import net.rebel459.unified.impl.builder.BlockSetProperties;
import net.rebel459.unified.impl.builder.EquipmentSetProperties;
import net.rebel459.unified.impl.builder.WoodSetProperties;
import net.rebel459.unified.impl.helper.StructureMusicImpl;

public class Unified {

    public static void initRegistries() {
        UnifiedDataComponents.init();
    }

    public static void init() {
        StructureMusicImpl.init();
        if (UnifiedInstance.getModLoader() == ModLoader.NEOFORGE) {
            WoodSetProperties.init(WoodSet.WOOD_SETS);
            BlockSetProperties.init(BlockSet.BLOCK_SETS);
            EquipmentSetProperties.init(EquipmentSet.EQUIPMENT_SETS);
        }
    }

    public static final String MOD_ID = "unified";
}