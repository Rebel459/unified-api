package net.rebel459.unified;

import net.rebel459.unified.platform.UnifiedPlatform;
import net.rebel459.unified.platform.InternalHandlerImpl;
import net.rebel459.unified.registry.*;
import net.rebel459.unified.util.codec.ExtensibleCodecs;
import net.rebel459.unified.util.data.registry.*;
import net.rebel459.unified.util.data.BiomeModifiers;
import net.rebel459.unified.util.LoaderType;
import net.rebel459.unified.util.builder.*;
import net.rebel459.unified.util.builder.impl.*;
import net.rebel459.unified.util.data.*;
import net.rebel459.unified.util.helper.impl.StructureMusicImpl;
import net.rebel459.unified.util.registry.RegistryResourceInitializer;
import net.rebel459.unified.util.registry.RegistryResourceListener;

import java.util.ServiceLoader;

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
