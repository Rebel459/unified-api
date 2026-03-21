package net.rebel459.unified.platform;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Arrays;

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
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((minecraftServer, closeableResourceManager, b) -> UnifiedEvents.Servers.passOnDatapackLoad(minecraftServer));
        ServerLifecycleEvents.SERVER_STARTED.register(UnifiedEvents.Servers::passOnStart);
        ServerLifecycleEvents.SERVER_STOPPED.register(UnifiedEvents.Servers::passOnStop);
        LootTableEvents.MODIFY.register((targetTable, tableBuilder, source, registries) -> {
            UnifiedEvents.LootTables.passModify(targetTable, tableBuilder::withPool, registries);
        });
    }
}
