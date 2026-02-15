package net.rebel459.unified.platform.client;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

public class NeoForgeUnifiedClientEvents {
    public static void init() {

        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Pre event) -> {
            UnifiedClientEvents.Tick.passStart(Minecraft.getInstance());
        });
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> {
            UnifiedClientEvents.Tick.passEnd(Minecraft.getInstance());
        });
    }
}
