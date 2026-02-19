package net.rebel459.unified.platform;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;

public class FabricUnifiedEvents {
    public static void init() {
        DefaultItemComponentEvents.MODIFY.register(context -> {
            context.modify(
                    item -> true,
                    (builder, item) -> UnifiedEvents.ItemComponents.passModify(item, builder)
            );
        });

        ServerPlayerEvents.JOIN.register(UnifiedEvents.Players::passOnJoin);
        ServerPlayerEvents.LEAVE.register(UnifiedEvents.Players::passOnLeave);
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, player, alive) -> UnifiedEvents.Players.passOnRespawn(player));
        CommandRegistrationCallback.EVENT.register(UnifiedEvents.Commands::passRegister);
    }
}
