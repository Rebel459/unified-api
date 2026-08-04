package net.rebel459.unified.neoforge.client.core;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.rebel459.unified.api.event.EventTiming;
import net.rebel459.unified.impl.client.core.CommonClientEvents;

public class NeoForgeUnifiedClientEvents {
    public static void init() {

        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Pre event) -> {
            CommonClientEvents.Instance.passOnTick(EventTiming.PRE, Minecraft.getInstance());
        });
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> {
            CommonClientEvents.Instance.passOnTick(EventTiming.POST, Minecraft.getInstance());
        });
        NeoForge.EVENT_BUS.addListener((ClientStartedEvent event) -> {
            CommonClientEvents.Instance.passOnStart(event.getClient());
        });
        NeoForge.EVENT_BUS.addListener((ClientStoppingEvent event) -> {
            CommonClientEvents.Instance.passOnStop(event.getClient());
        });
    }
}
