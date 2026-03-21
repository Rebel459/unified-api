package net.rebel459.unified;

import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.rebel459.unified.platform.HelpersImpl;
import net.rebel459.unified.platform.UnifiedEvents;
import net.rebel459.unified.platform.UnifiedHelpers;
import net.rebel459.unified.registry.UnifiedDataComponents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Unified {

    public static void initRegistries() {
        UnifiedDataComponents.init();
        UnifiedHelpers.BIOME_MODIFICATIONS.register(BiomeTags.HAS_DESERT_PYRAMID, context -> {
            context.getClimate().setPrecipitation(true);
            context.getEffects().setWaterColor(150);
        });
    }

    public static void init() {}

    public static final String MOD_ID = "unified";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
}
