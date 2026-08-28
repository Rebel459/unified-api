package net.rebel459.unified;

import net.rebel459.unified.api.builder.*;
import net.rebel459.unified.api.core.UnifiedInstance;
import net.rebel459.unified.api.platform.ModLoader;
import net.rebel459.unified.impl.builder.*;
import net.rebel459.unified.impl.helper.StructureMusicImpl;
import net.rebel459.unified.impl.registry.ComponentModifiers;
import net.rebel459.unified.impl.registry.LootInjections;
import net.rebel459.unified.impl.registry.MobVariants;

public class Unified {

    public static void initRegistries() {
        ExtensibleCodecs.init();
        UnifiedDataComponents.init();
        LootInjections.init();
        ComponentModifiers.init();
        MobVariants.init();
        BiomeModifiers.init();
        VanillaItemTypes.init();
        VanillaBlockTypes.init();
        VanillaStatePredicateTypes.init();
        VanillaMapColorTypes.init();
        VanillaLightEmissionTypes.init();
        VanillaPostProcessTypes.init();
        UnifiedStatePredicateTypes.init();
        UnifiedPostProcessTypes.init();
        new SoundEventRegistry().init();
        new BlockSetTypeRegistry().init();
        new WoodTypeRegistry().init();
        new BlockRegistry().init();
        new EntityTypeRegistry().init();
        new ItemRegistry().init();
        ServiceLoader.load(RegistryResourceInitializer.class, Unified.class.getClassLoader())
                .forEach(RegistryResourceInitializer::initializeRegistryResources);
        RegistryResourceListener.completeRegistration();
        InternalHandlerImpl.INSTANCE.impl().finishStaticRegistryBootstrap();
    }

    public static void init() {
        StructureMusicImpl.init();
        if (UnifiedInstance.getModLoader() == ModLoader.NEOFORGE) {
            WoodSetProperties.init(WoodSet.WOOD_SETS);
            BlockSetProperties.init(BlockSet.BLOCK_SETS);
            EquipmentSetProperties.init(EquipmentSet.EQUIPMENT_SETS);
            ColoredBlockSetProperties.init(ColoredBlockSet.COLORED_BLOCK_SETS);
            ColoredItemSetProperties.init(ColoredItemSet.COLORED_ITEM_SETS);
        }
    }

    public static final String MOD_ID = "unified";
}
