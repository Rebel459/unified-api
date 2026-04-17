package net.rebel459.unified.platform;

import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.rebel459.unified.util.EventType;
import net.rebel459.unified.util.LootEntry;
import net.rebel459.unified.util.event.LootTableProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class FabricUnifiedEvents {
    public static void init() {
        DefaultItemComponentEvents.MODIFY.register(context -> {
            context.modify(
                    item -> true,
                    (builder, provider, item) -> UnifiedEvents.DefaultDataComponents.passModify(item, builder, provider)
            );
        });
        ServerPlayerEvents.JOIN.register(UnifiedEvents.Players::passOnJoin);
        ServerPlayerEvents.LEAVE.register(UnifiedEvents.Players::passOnLeave);
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> EventsImpl.Players.passOnRespawn(oldPlayer, newPlayer));
        CommandRegistrationCallback.EVENT.register(UnifiedEvents.Commands::passRegister);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((minecraftServer, closeableResourceManager, b) -> UnifiedEvents.Server.passOnDatapackLoad(minecraftServer));
        ServerLifecycleEvents.SERVER_STARTED.register(UnifiedEvents.Server::passOnStart);
        ServerLifecycleEvents.SERVER_STOPPED.register(UnifiedEvents.Server::passOnStop);
        LootTableEvents.MODIFY.register((targetTable, tableBuilder, source, registries) -> {
            UnifiedEvents.LootTables.passModify(targetTable, new EventsImpl.LootTables.LootTable() {
                @Override
                public void addPool(LootPool.Builder pool) {
                    tableBuilder.withPool(pool);
                }

                @Override
                public void editPool(Predicate<Item> predicate, LootEntry entry) {
                    switch (entry.type()) {
                        case INSERT -> {
                            if (entry.entry().isEmpty()) {
                                LogUtils.getLogger().warn("Invalid UnifiedLootEntry. Type INSERT requires a LootPoolEntryContainer.Builder<?>");
                                return;
                            }
                            LootPoolEntryContainer builtEntry = entry.entry().get().build();

                            tableBuilder.modifyPools(pool -> {
                                List<LootPoolEntryContainer> entries = new ArrayList<>(LootTableProvider.getEntries(pool));
                                boolean matchesPool = entries.stream().anyMatch(existing -> EventsImpl.LootTables.matches(existing, predicate));
                                if (!matchesPool) {
                                    return;
                                }

                                entries.add(builtEntry);
                                pool.entries = LootTableProvider.immutableBuilder(entries);
                            });
                        }
                        case REPLACE -> {
                            if (entry.entry().isEmpty()) {
                                LogUtils.getLogger().warn("Invalid UnifiedLootEntry. Type REPLACE requires a LootPoolEntryContainer.Builder<?>");
                                return;
                            }
                            LootPoolEntryContainer builtEntry = entry.entry().get().build();

                            tableBuilder.modifyPools(pool -> {
                                List<LootPoolEntryContainer> entries = new ArrayList<>(LootTableProvider.getEntries(pool));
                                boolean matchesPool = entries.stream().anyMatch(existing -> EventsImpl.LootTables.matches(existing, predicate));
                                if (!matchesPool) {
                                    return;
                                }

                                EventsImpl.LootTables.handlePoolReplacements(entries, predicate, builtEntry, pool);
                            });
                        }
                        case REMOVE -> tableBuilder.modifyPools(pool -> {
                            List<LootPoolEntryContainer> entries = new ArrayList<>(LootTableProvider.getEntries(pool));
                            EventsImpl.LootTables.handlePoolRemovals(entries, predicate, pool);
                        });
                    }
                }

                @Override
                @Deprecated
                public void editPool(Predicate<Item> itemPredicate, LootPoolEntryContainer.Builder<?> entry, boolean replace) {
                    editPool(itemPredicate, replace ? LootEntry.replace(entry) : LootEntry.insert(entry));
                }
            }, registries);
        });
        ServerTickEvents.START_SERVER_TICK.register((server) -> UnifiedEvents.Server.passOnTick(EventType.PRE, server));
        ServerTickEvents.END_SERVER_TICK.register((server) -> UnifiedEvents.Server.passOnTick(EventType.POST, server));
        ServerTickEvents.START_LEVEL_TICK.register((level) -> UnifiedEvents.Server.passOnLevelTick(EventType.PRE, level));
        ServerTickEvents.END_LEVEL_TICK.register((level) -> UnifiedEvents.Server.passOnLevelTick(EventType.POST, level));
        ServerLivingEntityEvents.AFTER_DEATH.register(UnifiedEvents.Entities::passOnDeath);
        ServerEntityEvents.EQUIPMENT_CHANGE.register(UnifiedEvents.Entities::passOnEquipmentChange);
    }
}
