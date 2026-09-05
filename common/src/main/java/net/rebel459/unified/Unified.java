package net.rebel459.unified;

import net.minecraft.resources.Identifier;
import net.rebel459.unified.api.builder.*;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.core.RegistryResourceInitializer;
import net.rebel459.unified.api.core.RegistryResourceListener;
import net.rebel459.unified.api.core.UnifiedHelpers;
import net.rebel459.unified.impl.core.StaticRegistryBootstrap;
import net.rebel459.unified.api.core.UnifiedInstance;
import net.rebel459.unified.api.platform.ModLoader;
import net.rebel459.unified.api.registry.*;
import net.rebel459.unified.impl.builder.*;
import net.rebel459.unified.impl.data.*;
import net.rebel459.unified.impl.network.StructurePacketImpl;
import net.rebel459.unified.impl.registry.*;

import java.util.ServiceLoader;

public class Unified {

    public static void initRegistries() {
        ExtensibleCodecs.init();
        LootInjections.init();
        ComponentModifiers.init();
        MobVariants.init();
        BiomeModifiers.init();
        VanillaItemTypes.init();
        VanillaBlockTypes.init();
        VanillaBlockPredicateTypes.init();
        VanillaMapColorTypes.init();
        VanillaLightEmissionTypes.init();
        VanillaPostProcessTypes.init();
        UnifiedBlockPredicateTypes.init();
        UnifiedPostProcessTypes.init();
        UnifiedRequirementTypes.init();
        UnifiedItemPredicateTypes.init();
        UnifiedUseContextTypes.init();
        new SoundEventRegistry().init();
        new BlockSetTypeRegistry().init();
        new WoodTypeRegistry().init();
        new BlockRegistry().init();
        new EntityTypeRegistry().init();
        new ItemRegistry().init();
        ServiceLoader.load(RegistryResourceInitializer.class, Unified.class.getClassLoader()).forEach(RegistryResourceInitializer::initializeRegistryResources);
        RegistryResourceListener.completeRegistration();
        StaticRegistryBootstrap.finish();
        UnifiedHelpers.RELOAD_LISTENERS.addListener(BlockConversions.ID, new BlockConversions());
    }

    public static void init() {
        StructurePacketImpl.init();
        if (UnifiedInstance.getModLoader() == ModLoader.NEOFORGE) {
            WoodSetProperties.init(WoodSet.WOOD_SETS);
            BlockSetProperties.init(BlockSet.BLOCK_SETS);
            EquipmentSetProperties.init(EquipmentSet.EQUIPMENT_SETS);
            ColoredBlockSetProperties.init(ColoredBlockSet.COLORED_BLOCK_SETS);
            ColoredItemSetProperties.init(ColoredItemSet.COLORED_ITEM_SETS);
        }
    }

    public static final String MOD_ID = "unified";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
