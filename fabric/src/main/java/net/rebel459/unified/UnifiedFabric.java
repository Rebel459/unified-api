package net.rebel459.unified;

import net.fabricmc.api.ModInitializer;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.rebel459.unified.platform.*;

public class UnifiedFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Unified.initRegistries();
        FabricUnifiedEvents.init();
        Unified.init();
    }
}
