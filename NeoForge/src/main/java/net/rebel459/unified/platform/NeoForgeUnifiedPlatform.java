package net.rebel459.unified.platform;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.util.ArrayList;
import java.util.List;

public class NeoForgeUnifiedPlatform implements UnifiedPlatform {

    @Override
    public String getPlatform() {
        return UnifiedPlatform.NEOFORGE;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}