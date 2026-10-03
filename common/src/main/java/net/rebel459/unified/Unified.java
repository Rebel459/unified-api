package net.rebel459.unified;

import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.rebel459.unified.api.builder.*;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.core.RegistryResourceInitializer;
import net.rebel459.unified.api.core.RegistryResourceListener;
import net.rebel459.unified.api.core.UnifiedHelpers;
import net.rebel459.unified.api.core.StagedRegistry;
import net.rebel459.unified.api.core.UnifiedPlatform;
import net.rebel459.unified.api.platform.ModLoader;
import net.rebel459.unified.api.registry.*;
import net.rebel459.unified.impl.builder.*;
import net.rebel459.unified.impl.data.helper.*;
import net.rebel459.unified.impl.data.registry.*;
import net.rebel459.unified.impl.network.StructurePacketImpl;

import java.util.ServiceLoader;

public class Unified {

    private static boolean codecsInitialized;

    public static synchronized void initCodecs() {
        if (codecsInitialized) return;
        ExtensibleCodecs.init();
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
        codecsInitialized = true;
    }

    public static void initRegistries() {
        initCodecs();
        UnifiedDataComponents.init();
        LootInjections.init();
        ComponentModifiers.init();
        MobVariants.init();
        BiomeModifiers.init();
        new SoundEventRegistry().init();
        new BlockSetTypeRegistry().init();
        new WoodTypeRegistry().init();
        new BlockRegistry().init();
        new EntityRegistry().init();
        new ItemRegistry().init();
        new CreativeTabRegistry().init();
        ServiceLoader.load(RegistryResourceInitializer.class, Unified.class.getClassLoader()).forEach(RegistryResourceInitializer::initializeRegistryResources);
        UnifiedHelpers.RELOAD_LISTENERS.addListener(BlockConversions.ID, new BlockConversions());
    }

    /** Called by the loader after mod registry initialization, before static registries close. */
    public static void completeRegistries() {
        // Block/item definitions can decode particles, attributes and data components registered
        // by mods. Fabric commits these now; NeoForge queues them for its earlier RegisterEvents.
        StagedRegistry.finish(Registries.ATTRIBUTE);
        StagedRegistry.finish(Registries.DATA_COMPONENT_TYPE);
        StagedRegistry.finish(Registries.PARTICLE_TYPE);
        RegistryResourceListener.completeRegistration();
        StagedRegistry.finish();
    }

    public static void init() {
        BlockRegistry.runLateProperties();
        StructurePacketImpl.init();
        if (UnifiedPlatform.getModLoader() == ModLoader.NEOFORGE) {
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
