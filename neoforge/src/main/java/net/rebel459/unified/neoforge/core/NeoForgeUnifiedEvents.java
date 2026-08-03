package net.rebel459.unified.neoforge.core;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.rebel459.unified.api.event.EventTiming;
import net.rebel459.unified.impl.core.EventsImpl;
import net.rebel459.unified.impl.event.LootTableProvider;

import java.util.ArrayList;
import java.util.List;

public class NeoForgeUnifiedEvents {

    public static void init(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) EventsImpl.Players.passOnJoin(player);
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) EventsImpl.Players.passOnLeave(player);
        });

        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
            EventsImpl.Commands.passRegister(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
        });

        NeoForge.EVENT_BUS.addListener((TagsUpdatedEvent.ServerDataLoad event) -> {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) EventsImpl.Server.passOnDatapackLoad(server);
        });

        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> {
            EventsImpl.Server.passOnStart(event.getServer());
        });
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> {
            EventsImpl.Server.passOnStop(event.getServer());
        });

        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, (LootTableLoadEvent event) -> {
            LootTable originalTable = event.getTable();
            List<LootPool.Builder> pools = new ArrayList<>();
            for (LootPool pool : originalTable.pools) {
                LootPool.Builder builder = LootPool.lootPool();
                builder.entries = LootTableProvider.immutableBuilder(pool.entries);
                builder.conditions = LootTableProvider.immutableBuilder(pool.conditions);
                builder.functions = LootTableProvider.immutableBuilder(pool.functions);
                builder.rolls = pool.rolls;
                builder.bonusRolls = pool.bonusRolls;
                pools.add(builder);
            }

            boolean changed = EventsImpl.LootTables.passModify(event.getKey(), new EventsImpl.LootTables.PoolAccess() {
                private boolean changed;

                @Override
                public List<LootPool.Builder> pools() {
                    return pools;
                }

                @Override
                public void addPool(LootPool.Builder pool) {
                    pools.add(pool);
                    this.changed = true;
                }

                @Override
                public void markChanged() {
                    this.changed = true;
                }

                @Override
                public boolean hasChanged() {
                    return this.changed;
                }
            }, event.getRegistries());

            if (!changed) {
                return;
            }

            LootTable.Builder rebuilt = LootTable.lootTable().setParamSet(originalTable.getParamSet());
            originalTable.randomSequence.ifPresent(rebuilt::setRandomSequence);
            for (LootPool.Builder pool : pools) {
                rebuilt.withPool(pool);
            }
            rebuilt.functions = LootTableProvider.immutableBuilder(originalTable.functions);
            event.setTable(rebuilt.build());
        });

        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Pre event) -> {
            EventsImpl.Server.passOnTick(EventTiming.PRE, event.getServer());
        });
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
            EventsImpl.Server.passOnTick(EventTiming.POST, event.getServer());
        });
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Pre event) -> {
            event.getServer().getAllLevels().forEach(level -> EventsImpl.Server.passOnLevelTick(EventTiming.PRE, level));
            event.getServer().getAllLevels().forEach(level -> EventsImpl.Levels.passOnTick(EventTiming.PRE, level));
        });
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
            event.getServer().getAllLevels().forEach(level -> EventsImpl.Server.passOnLevelTick(EventTiming.POST, level));
            event.getServer().getAllLevels().forEach(level -> EventsImpl.Levels.passOnTick(EventTiming.POST, level));
        });
        NeoForge.EVENT_BUS.addListener((LivingDeathEvent event) -> {
            EventsImpl.Entities.passOnDeath(event.getEntity(), event.getSource());
        });
        NeoForge.EVENT_BUS.addListener((LivingEquipmentChangeEvent event) -> {
            EventsImpl.Entities.passOnEquipmentChange(event.getEntity(), event.getSlot(), event.getFrom(), event.getTo());
        });

        NeoForge.EVENT_BUS.addListener((LevelEvent.Load event) -> {
            if (event.getLevel() instanceof ServerLevel level) EventsImpl.Server.passOnLevelLoad(level);
            if (event.getLevel() instanceof Level level && !level.isClientSide()) EventsImpl.Levels.passOnLoad(level);
        });
        NeoForge.EVENT_BUS.addListener((LevelEvent.Unload event) -> {
            if (event.getLevel() instanceof ServerLevel level) EventsImpl.Server.passOnLevelUnload(level);
            if (event.getLevel() instanceof Level level && !level.isClientSide()) EventsImpl.Levels.passOnUnload(level);
        });

        modEventBus.addListener((ModifyDefaultComponentsEvent event) -> {
            event.modifyMatching((_, _) -> true, (builder, provider, item) -> EventsImpl.DefaultDataComponents.passModify(item, builder, provider));
        });
    }
}
