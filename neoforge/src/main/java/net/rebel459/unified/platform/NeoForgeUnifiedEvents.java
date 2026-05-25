package net.rebel459.unified.platform;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;
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
import net.rebel459.unified.util.EventType;
import net.rebel459.unified.util.event.LootTableProvider;

import java.util.ArrayList;
import java.util.List;

public class NeoForgeUnifiedEvents {

    public static void init(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) UnifiedEvents.Players.passOnJoin(player);
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) UnifiedEvents.Players.passOnLeave(player);
        });

        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
            UnifiedEvents.Commands.passRegister(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
        });

        NeoForge.EVENT_BUS.addListener((TagsUpdatedEvent event) -> {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD && server != null) {
                UnifiedEvents.Server.passOnDatapackLoad(server);
            }
        });

        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> {
            UnifiedEvents.Server.passOnStart(event.getServer());
        });
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> {
            UnifiedEvents.Server.passOnStop(event.getServer());
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

            boolean changed = UnifiedEvents.LootTables.passModify(event.getKey(), new UnifiedEvents.LootTables.PoolAccess() {
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
            UnifiedEvents.Server.passOnTick(EventType.PRE, event.getServer());
        });
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
            UnifiedEvents.Server.passOnTick(EventType.POST, event.getServer());
        });
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Pre event) -> {
            event.getServer().getAllLevels().forEach(level -> UnifiedEvents.Server.passOnLevelTick(EventType.PRE, level));
        });
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
            event.getServer().getAllLevels().forEach(level -> UnifiedEvents.Server.passOnLevelTick(EventType.POST, level));
        });
        NeoForge.EVENT_BUS.addListener((LivingDeathEvent event) -> {
            UnifiedEvents.Entities.passOnDeath(event.getEntity(), event.getSource());
        });
        NeoForge.EVENT_BUS.addListener((LivingEquipmentChangeEvent event) -> {
            UnifiedEvents.Entities.passOnEquipmentChange(event.getEntity(), event.getSlot(), event.getFrom(), event.getTo());
        });

        NeoForge.EVENT_BUS.addListener((LevelEvent.Load event) -> {
            if (event.getLevel() instanceof ServerLevel level) EventsImpl.Server.passOnLevelLoad(level);
            if (event.getLevel() instanceof Level level && !level.isClientSide()) EventsImpl.Levels.passOnLoad(level);
        });
        NeoForge.EVENT_BUS.addListener((LevelEvent.Unload event) -> {
            if (event.getLevel() instanceof ServerLevel level) EventsImpl.Server.passOnLevelUnload(level);
            if (event.getLevel() instanceof Level level && !level.isClientSide()) EventsImpl.Levels.passOnUnload(level);
        });
    }

    public static final ScopedValue<HolderLookup.Provider> DEFAULT_ITEM_COMPONENTS_LOOKUP_PROVIDER = ScopedValue.newInstance();

    public static void modifyDefaultItemComponentsEvent(HolderLookup.Provider provider) {
        for (Item item : BuiltInRegistries.ITEM) {
            DataComponentMap.Builder builder = DataComponentMap.builder().addAll(item.components());
            UnifiedEvents.DefaultDataComponents.passModify(item, builder, provider);
            item.builtInRegistryHolder().bindComponents(builder.build());
        }
    }
}
