package net.rebel459.unified.platform.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class FabricUnifiedClientEvents {
    public static void init() {

        ClientTickEvents.START_CLIENT_TICK.register(UnifiedClientEvents.Ticks::passAtStart);
        ClientTickEvents.END_CLIENT_TICK.register(UnifiedClientEvents.Ticks::passAtEnd);
    }
}
