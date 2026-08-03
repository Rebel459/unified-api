package net.rebel459.unified.neoforge.client.core;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.rebel459.unified.api.event.EventTiming;
import net.rebel459.unified.api.client.core.UnifiedClientEvents;
import net.rebel459.unified.impl.client.core.ClientEventsImpl;

public class NeoForgeUnifiedClientEvents {
    public static void init() {

        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Pre event) -> {
            ClientEventsImpl.Instance.passOnTick(EventTiming.PRE, Minecraft.getInstance());
        });
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> {
            ClientEventsImpl.Instance.passOnTick(EventTiming.POST, Minecraft.getInstance());
        });
        NeoForge.EVENT_BUS.addListener((ClientStartedEvent event) -> {
            ClientEventsImpl.Instance.passOnStart(event.getClient());
        });
        NeoForge.EVENT_BUS.addListener((ClientStoppingEvent event) -> {
            ClientEventsImpl.Instance.passOnStop(event.getClient());
        });
    }
}
