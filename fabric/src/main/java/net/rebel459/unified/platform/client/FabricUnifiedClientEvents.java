package net.rebel459.unified.platform.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.rebel459.unified.util.EventType;

public class FabricUnifiedClientEvents {
    public static void init() {

        ClientTickEvents.START_CLIENT_TICK.register((client) -> UnifiedClientEvents.Instance.passOnTick(EventType.PRE, client));
        ClientTickEvents.END_CLIENT_TICK.register((client) -> UnifiedClientEvents.Instance.passOnTick(EventType.POST, client));
    }
}
