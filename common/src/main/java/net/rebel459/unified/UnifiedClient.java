package net.rebel459.unified;

import net.rebel459.unified.client.registry.builder.impl.WoodsetClientImpl;
import net.rebel459.unified.client.util.helper.impl.LegacyBabyArmorImpl;

public class UnifiedClient {

    public static void init() {
        LegacyBabyArmorImpl.init();
        WoodsetClientImpl.init();
    }
}
