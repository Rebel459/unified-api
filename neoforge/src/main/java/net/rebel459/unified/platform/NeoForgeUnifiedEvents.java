package net.rebel459.unified.platform;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.rebel459.unified.util.loot.LootTableProvider;

import java.util.ArrayList;
import java.util.List;

public class NeoForgeUnifiedEvents {

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener((ModifyDefaultComponentsEvent event) -> {
            BuiltInRegistries.ITEM.forEach(item -> {
                event.modify(item, builder -> {
                    UnifiedEvents.DefaultItemComponents.passModify(item, builder);
                });
            });
        });

        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            UnifiedEvents.Players.passOnJoin(event.getEntity());
        });

        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            UnifiedEvents.Players.passOnLeave(event.getEntity());
        });

        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
            UnifiedEvents.Commands.passRegister(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
        });

        NeoForge.EVENT_BUS.addListener((TagsUpdatedEvent event) -> {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD && server != null) {
                UnifiedEvents.Servers.passOnDatapackLoad(server);
            }
        });

        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> {
            UnifiedEvents.Servers.passOnStart(event.getServer());
        });

        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> {
            UnifiedEvents.Servers.passOnStop(event.getServer());
        });

        NeoForge.EVENT_BUS.addListener((LootTableLoadEvent event) -> {
            LootTable originalTable = event.getTable();
            List<LootPool.Builder> pools = new ArrayList<>();
            for (LootPool pool : LootTableProvider.getPools(originalTable)) {
                LootPool.Builder builder = LootPool.lootPool();
                LootTableProvider.setEntries(builder, LootTableProvider.getEntries(pool));
                LootTableProvider.setConditions(builder, LootTableProvider.getConditions(pool));
                LootTableProvider.setFunctions(builder, LootTableProvider.getFunctions(pool));
                LootTableProvider.setRolls(builder, LootTableProvider.getRolls(pool));
                LootTableProvider.setBonusRolls(builder, LootTableProvider.getBonusRolls(pool));
                pools.add(builder);
            }

            UnifiedEvents.LootTables.passModify(event.getKey(), new UnifiedEvents.LootTables.PoolAccess() {
                @Override
                public List<LootPool.Builder> pools() {
                    return pools;
                }

                @Override
                public void addPool(LootPool.Builder pool) {
                    pools.add(pool);
                }
            }, event.getRegistries());

            LootTable.Builder rebuilt = LootTable.lootTable().setParamSet(originalTable.getParamSet());
            LootTableProvider.getRandomSequence(originalTable).ifPresent(rebuilt::setRandomSequence);
            for (LootPool.Builder pool : pools) {
                rebuilt.withPool(pool);
            }
            LootTableProvider.setFunctions(rebuilt, LootTableProvider.getFunctions(originalTable));
            event.setTable(rebuilt.build());
        });

        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Pre event) -> {
            UnifiedEvents.Servers.passOnTickStart(event.getServer());
        });
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
            UnifiedEvents.Servers.passOnTickEnd(event.getServer());
        });

        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Pre event) -> {
            event.getServer().getAllLevels().forEach(UnifiedEvents.Servers::passOnLevelTickStart);
        });
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
            event.getServer().getAllLevels().forEach(UnifiedEvents.Servers::passOnLevelTickEnd);
        });
    }
}
