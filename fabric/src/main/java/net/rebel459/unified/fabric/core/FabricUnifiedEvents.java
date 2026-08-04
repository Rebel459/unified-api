package net.rebel459.unified.fabric.core;

import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.rebel459.unified.api.event.EventTiming;
import net.rebel459.unified.api.event.LootEntry;
import net.rebel459.unified.api.event.LootTableContext;
import net.rebel459.unified.impl.core.CommonEvents;
import net.rebel459.unified.impl.event.LootTableProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class FabricUnifiedEvents {
    public static void init() {
        DefaultItemComponentEvents.MODIFY.register(context -> {
            context.modify(
                    item -> true,
                    (builder, provider, item) -> CommonEvents.DefaultDataComponents.passModify(item, builder, provider)
            );
        });
        ServerPlayerEvents.JOIN.register(CommonEvents.Players::passOnJoin);
        ServerPlayerEvents.LEAVE.register(CommonEvents.Players::passOnLeave);
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> CommonEvents.Players.passOnRespawn(oldPlayer, newPlayer));
        CommandRegistrationCallback.EVENT.register(CommonEvents.Commands::passRegister);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((minecraftServer, closeableResourceManager, b) -> CommonEvents.Server.passOnDatapackLoad(minecraftServer));
        ServerLifecycleEvents.SERVER_STARTED.register(CommonEvents.Server::passOnStart);
        ServerLifecycleEvents.SERVER_STOPPED.register(CommonEvents.Server::passOnStop);
        LootTableEvents.MODIFY.register((targetTable, tableBuilder, source, registries) -> {
            CommonEvents.LootTables.passModify(targetTable, new LootTableContext() {
                @Override
                public void addPool(LootPool.Builder pool) {
                    tableBuilder.withPool(pool);
                }

                @Override
                public void editPool(Predicate<Item> predicate, LootEntry entry) {
                    switch (entry.getType()) {
                        case INSERT -> {
                            if (entry.getEntry().isEmpty()) {
                                LogUtils.getLogger().warn("Invalid UnifiedLootEntry. Type INSERT requires a LootPoolEntryContainer.Builder<?>");
                                return;
                            }
                            LootPoolEntryContainer builtEntry = entry.getEntry().get().build();

                            tableBuilder.modifyPools(pool -> {
                                List<LootPoolEntryContainer> entries = new ArrayList<>(LootTableProvider.getEntries(pool));
                                boolean matchesPool = entries.stream().anyMatch(existing -> CommonEvents.LootTables.matches(existing, predicate));
                                if (!matchesPool) {
                                    return;
                                }

                                entries.add(builtEntry);
                                pool.entries = LootTableProvider.immutableBuilder(entries);
                            });
                        }
                        case REPLACE -> {
                            if (entry.getEntry().isEmpty()) {
                                LogUtils.getLogger().warn("Invalid UnifiedLootEntry. Type REPLACE requires a LootPoolEntryContainer.Builder<?>");
                                return;
                            }
                            LootPoolEntryContainer builtEntry = entry.getEntry().get().build();

                            tableBuilder.modifyPools(pool -> {
                                List<LootPoolEntryContainer> entries = new ArrayList<>(LootTableProvider.getEntries(pool));
                                boolean matchesPool = entries.stream().anyMatch(existing -> CommonEvents.LootTables.matches(existing, predicate));
                                if (!matchesPool) {
                                    return;
                                }

                                CommonEvents.LootTables.handlePoolReplacements(entries, predicate, builtEntry, pool);
                            });
                        }
                        case REMOVE -> tableBuilder.modifyPools(pool -> {
                            List<LootPoolEntryContainer> entries = new ArrayList<>(LootTableProvider.getEntries(pool));
                            CommonEvents.LootTables.handlePoolRemovals(entries, predicate, pool);
                        });
                    }
                }
            }, registries);
        });
        ServerTickEvents.START_SERVER_TICK.register((server) -> CommonEvents.Server.passOnTick(EventTiming.PRE, server));
        ServerTickEvents.END_SERVER_TICK.register((server) -> CommonEvents.Server.passOnTick(EventTiming.POST, server));
        ServerTickEvents.START_LEVEL_TICK.register((level) -> {
            CommonEvents.Server.passOnLevelTick(EventTiming.PRE, level);
            CommonEvents.Levels.passOnTick(EventTiming.PRE, level);
        });
        ServerTickEvents.END_LEVEL_TICK.register((level) -> {
            CommonEvents.Server.passOnLevelTick(EventTiming.POST, level);
            CommonEvents.Levels.passOnTick(EventTiming.POST, level);
        });
        ServerLivingEntityEvents.AFTER_DEATH.register(CommonEvents.Entities::passOnDeath);
        ServerEntityEvents.EQUIPMENT_CHANGE.register(CommonEvents.Entities::passOnEquipmentChange);
        ServerLevelEvents.LOAD.register(((server, level) -> {
            CommonEvents.Server.passOnLevelLoad(level);
            CommonEvents.Levels.passOnLoad(level);
        }));
        ServerLevelEvents.UNLOAD.register(((server, level) -> {
            CommonEvents.Server.passOnLevelUnload(level);
            CommonEvents.Levels.passOnUnload(level);
        }));
    }
}
