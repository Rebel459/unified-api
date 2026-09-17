package net.rebel459.unified;

import net.minecraft.resources.Identifier;
import net.rebel459.unified.api.builder.*;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.core.RegistryResourceInitializer;
import net.rebel459.unified.api.core.RegistryResourceListener;
import net.rebel459.unified.api.core.UnifiedHelpers;
import net.rebel459.unified.api.core.StagedRegistry;
import net.rebel459.unified.api.core.UnifiedInstance;
import net.rebel459.unified.api.platform.ModLoader;
import net.rebel459.unified.api.registry.*;
import net.rebel459.unified.impl.builder.*;
import net.rebel459.unified.impl.data.helper.*;
import net.rebel459.unified.impl.data.registry.*;
import net.rebel459.unified.impl.network.StructurePacketImpl;

import java.util.ServiceLoader;

public class Unified {

    public static void initRegistries() {
        ExtensibleCodecs.init();
        LootInjections.init();
        ComponentModifiers.init();
        MobVariants.init();
        BiomeModifiers.init();
        VanillaItemCodecs.init();
        VanillaBlockCodecs.init();
        VanillaBlockPredicateCodecs.init();
        VanillaMapColorCodecs.init();
        VanillaLightEmissionCodecs.init();
        VanillaPostProcessCodecs.init();
        VanillaSpawnPlacementCodecs.init();
        VanillaSpawnPredicateCodecs.init();
        VanillaEntityCodecs.init();
        UnifiedMapColorCodecs.init();
        UnifiedLightEmissionCodecs.init();
        UnifiedBlockPredicateCodecs.init();
        UnifiedPostProcessCodecs.init();
        UnifiedLoadRequirementCodecs.init();
        UnifiedItemPredicateCodecs.init();
        UnifiedUseContextCodecs.init();
        UnifiedDisplayItemCodecs.init();
        new SoundEventRegistry().init();
        new BlockSetTypeRegistry().init();
        new WoodTypeRegistry().init();
        new BlockRegistry().init();
        new EntityRegistry().init();
        new ItemRegistry().init();
        new CreativeTabRegistry().init();
        ServiceLoader.load(RegistryResourceInitializer.class, Unified.class.getClassLoader()).forEach(RegistryResourceInitializer::initializeRegistryResources);
        RegistryResourceListener.completeRegistration();
        StagedRegistry.finish();
        UnifiedHelpers.RELOAD_LISTENERS.addListener(BlockConversions.ID, new BlockConversions());
    }

    public static void init() {
        BlockRegistry.runLateProperties();
        StructurePacketImpl.init();
        if (UnifiedInstance.getModLoader() == ModLoader.NEOFORGE) {
            WoodSetProperties.init(WoodSet.WOOD_SETS);
            StoneSetProperties.init(StoneSet.BLOCK_SETS);
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
