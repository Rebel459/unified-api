package net.rebel459.unified;

import net.rebel459.unified.api.platform.ModLoader;
import net.rebel459.unified.impl.client.builder.WoodSetClientProperties;
import net.rebel459.unified.impl.client.helper.SimpleBabyArmorImpl;
import net.rebel459.unified.api.core.UnifiedInstance;

public class UnifiedClient {

    public static void init() {
        SimpleBabyArmorImpl.init();
        if (UnifiedInstance.getModLoader() == ModLoader.FABRIC) WoodSetClientProperties.init(true, true);
    }
}
