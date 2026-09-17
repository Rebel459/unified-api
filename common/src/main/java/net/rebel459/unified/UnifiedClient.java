package net.rebel459.unified;

import net.rebel459.unified.api.client.core.UnifiedClientHelpers;
import net.rebel459.unified.api.platform.ModLoader;
import net.rebel459.unified.impl.client.builder.WoodSetClientProperties;
import net.rebel459.unified.impl.client.helper.SimpleBabyArmorImpl;
import net.rebel459.unified.api.core.UnifiedInstance;
import net.rebel459.unified.impl.client.registry.VanillaEntityRenderers;
import net.rebel459.unified.impl.data.helper.CreativeEntries;
import net.rebel459.unified.impl.data.helper.SimpleBabyArmor;

public class UnifiedClient {

    public static void init() {
        SimpleBabyArmorImpl.init();
        if (UnifiedInstance.getModLoader() == ModLoader.FABRIC) WoodSetClientProperties.init(true, true);
        UnifiedClientHelpers.RELOAD_LISTENERS.addListener(CreativeEntries.ID, new CreativeEntries());
        UnifiedClientHelpers.RELOAD_LISTENERS.addListener(SimpleBabyArmor.ID, new SimpleBabyArmor());
        VanillaEntityRenderers.init();
    }
}
