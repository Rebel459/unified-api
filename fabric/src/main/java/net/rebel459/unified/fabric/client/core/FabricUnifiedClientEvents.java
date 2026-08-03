package net.rebel459.unified.fabric.client.core;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.rebel459.unified.api.event.EventTiming;
import net.rebel459.unified.api.client.core.UnifiedClientEvents;
import net.rebel459.unified.impl.client.core.ClientEventsImpl;

public class FabricUnifiedClientEvents {
    public static void init() {

        ClientTickEvents.START_CLIENT_TICK.register((client) -> ClientEventsImpl.Instance.passOnTick(EventTiming.PRE, client));
        ClientTickEvents.END_CLIENT_TICK.register((client) -> ClientEventsImpl.Instance.passOnTick(EventTiming.POST, client));
        ClientLifecycleEvents.CLIENT_STARTED.register(ClientEventsImpl.Instance::passOnStart);
        ClientLifecycleEvents.CLIENT_STOPPING.register(ClientEventsImpl.Instance::passOnStop);
    }
}
