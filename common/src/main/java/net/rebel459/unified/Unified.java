package net.rebel459.unified;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.core.RegistryResourceInitializer;
import net.rebel459.unified.api.core.RegistryResourceListener;
import net.rebel459.unified.api.core.StagedRegistry;
import net.rebel459.unified.api.core.UnifiedPlatform;
import net.rebel459.unified.api.data.set.*;
import net.rebel459.unified.api.platform.ModLoader;
import net.rebel459.unified.api.registry.*;
import net.rebel459.unified.impl.data.helper.*;
import net.rebel459.unified.impl.data.registry.*;
import net.rebel459.unified.impl.data.set.*;
import net.rebel459.unified.impl.network.StructurePacketImpl;
import net.rebel459.unified.impl.tag.PersistentCooldowns;

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
        LootInjections.init();
        BlockConversions.init();
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
    }

    public static void completeRegistries() {
        StagedRegistry.finish(Registries.ATTRIBUTE);
        StagedRegistry.finish(Registries.DATA_COMPONENT_TYPE);
        StagedRegistry.finish(Registries.PARTICLE_TYPE);
        RegistryResourceListener.completeRegistration();
        StagedRegistry.finish();
    }

    public static void init() {
        PersistentCooldowns.init();
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
