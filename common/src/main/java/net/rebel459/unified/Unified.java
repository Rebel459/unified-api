package net.rebel459.unified;

import net.rebel459.unified.platform.UnifiedPlatform;
import net.rebel459.unified.util.data.registry.BlockRegistry;
import net.rebel459.unified.util.data.registry.EntityRegistry;
import net.rebel459.unified.util.data.registry.ItemRegistry;
import net.rebel459.unified.util.data.BiomeModifiers;
import net.rebel459.unified.registry.UnifiedBlockTypes;
import net.rebel459.unified.registry.UnifiedDataComponents;
import net.rebel459.unified.registry.UnifiedItemTypes;
import net.rebel459.unified.util.LoaderType;
import net.rebel459.unified.util.builder.*;
import net.rebel459.unified.util.builder.impl.*;
import net.rebel459.unified.util.data.*;
import net.rebel459.unified.util.helper.impl.StructureMusicImpl;

public class Unified {

    public static void initRegistries() {
        UnifiedDataComponents.init();
        LootInjections.init();
        ComponentModifiers.init();
        MobVariants.init();
        BiomeModifiers.init();
        UnifiedItemTypes.init();
        UnifiedBlockTypes.init();
        new ItemRegistry().init();
        new BlockRegistry().init();
        new EntityRegistry().init();
    }

    public static void init() {
        StructureMusicImpl.init();
        if (UnifiedPlatform.getLoader() == LoaderType.NEOFORGE) {
            WoodSetImpl.init(WoodSet.WOOD_SETS);
            BlockSetImpl.init(BlockSet.BLOCK_SETS);
            EquipmentSetImpl.init(EquipmentSet.EQUIPMENT_SETS);
            ColoredBlockSetImpl.init(ColoredBlockSet.COLORED_BLOCK_SETS);
            ColoredItemSetImpl.init(ColoredItemSet.COLORED_ITEM_SETS);
        }
    }

    public static final String MOD_ID = "unified";
}
