package net.rebel459.unified.platform;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.level.storage.loot.LootPool;

import java.util.ArrayList;
import java.util.List;

public class FabricUnifiedEvents {
    public static void init() {
        DefaultItemComponentEvents.MODIFY.register(context -> {
            context.modify(
                    item -> true,
                    (builder, item) -> UnifiedEvents.DefaultItemComponents.passModify(item, builder)
            );
        });

        ServerPlayerEvents.JOIN.register(UnifiedEvents.Players::passOnJoin);
        ServerPlayerEvents.LEAVE.register(UnifiedEvents.Players::passOnLeave);
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> EventsImpl.Players.passOnRespawn(oldPlayer, newPlayer));
        CommandRegistrationCallback.EVENT.register(UnifiedEvents.Commands::passRegister);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((minecraftServer, closeableResourceManager, b) -> UnifiedEvents.Servers.passOnDatapackLoad(minecraftServer));
        ServerLifecycleEvents.SERVER_STARTED.register(UnifiedEvents.Servers::passOnStart);
        ServerLifecycleEvents.SERVER_STOPPED.register(UnifiedEvents.Servers::passOnStop);
        LootTableEvents.MODIFY.register((targetTable, tableBuilder, source, registries) -> {
            List<LootPool.Builder> pools = new ArrayList<>();
            tableBuilder.modifyPools(pools::add);
            UnifiedEvents.LootTables.passModify(targetTable, new UnifiedEvents.LootTables.PoolAccess() {
                @Override
                public List<LootPool.Builder> pools() {
                    return pools;
                }

                @Override
                public void addPool(LootPool.Builder pool) {
                    pools.add(pool);
                    tableBuilder.withPool(pool);
                }
            }, registries);
        });

        ServerTickEvents.START_SERVER_TICK.register(UnifiedEvents.Servers::passOnTickStart);
        ServerTickEvents.END_SERVER_TICK.register(UnifiedEvents.Servers::passOnTickStart);
        ServerTickEvents.START_LEVEL_TICK.register(UnifiedEvents.Servers::passOnLevelTickStart);
        ServerTickEvents.END_LEVEL_TICK.register(UnifiedEvents.Servers::passOnLevelTickEnd);
    }
}
