package net.rebel459.unified;

import net.rebel459.unified.client.util.builder.impl.WoodSetClientImpl;
import net.rebel459.unified.client.util.helper.impl.LegacyBabyArmorImpl;
import net.rebel459.unified.platform.UnifiedPlatform;
import net.rebel459.unified.util.LoaderType;

public class UnifiedClient {

    public static void init() {
        LegacyBabyArmorImpl.init();
        if (UnifiedPlatform.getLoader() == LoaderType.FABRIC) WoodSetClientImpl.init(true, true);
    }
}
