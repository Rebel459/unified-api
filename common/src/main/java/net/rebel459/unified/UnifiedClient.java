package net.rebel459.unified;

import net.rebel459.unified.api.platform.ModLoader;
import net.rebel459.unified.impl.client.builder.WoodSetClientImpl;
import net.rebel459.unified.impl.client.helper.LegacyBabyArmorImpl;
import net.rebel459.unified.api.core.UnifiedPlatform;

public class UnifiedClient {

    public static void init() {
        LegacyBabyArmorImpl.init();
        if (UnifiedPlatform.getModLoader() == ModLoader.FABRIC) WoodSetClientImpl.init(true, true);
    }
}
