package net.rebel459.unified.fabric.client.core;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.rebel459.unified.api.event.EventTiming;
import net.rebel459.unified.impl.client.core.CommonClientEvents;

public class FabricUnifiedClientEvents {
    public static void init() {

        ClientTickEvents.START_CLIENT_TICK.register((client) -> CommonClientEvents.Instance.passOnTick(EventTiming.PRE, client));
        ClientTickEvents.END_CLIENT_TICK.register((client) -> CommonClientEvents.Instance.passOnTick(EventTiming.POST, client));
        ClientLifecycleEvents.CLIENT_STARTED.register(CommonClientEvents.Instance::passOnStart);
        ClientLifecycleEvents.CLIENT_STOPPING.register(CommonClientEvents.Instance::passOnStop);
    }
}
