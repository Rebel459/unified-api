package net.rebel459.unified.neoforge.core;

import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.*;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.rebel459.unified.api.event.CreativeEntryContext;
import net.rebel459.unified.api.event.EventTiming;
import net.rebel459.unified.impl.core.CommonEvents;
import net.rebel459.unified.impl.event.LootTableProvider;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class NeoForgeUnifiedEvents {

    public static void init(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) CommonEvents.Players.passOnJoin(player);
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) CommonEvents.Players.passOnLeave(player);
        });

        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
            CommonEvents.Commands.passRegister(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
        });

        NeoForge.EVENT_BUS.addListener((TagsUpdatedEvent.ServerDataLoad event) -> {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) CommonEvents.Server.passOnDatapackLoad(server);
        });

        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> {
            CommonEvents.Server.passOnStart(event.getServer());
        });
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> {
            CommonEvents.Server.passOnStop(event.getServer());
        });

        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Pre event) -> {
            CommonEvents.Server.passOnTick(EventTiming.PRE, event.getServer());
        });
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
            CommonEvents.Server.passOnTick(EventTiming.POST, event.getServer());
        });
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Pre event) -> {
            event.getServer().getAllLevels().forEach(level -> {
                CommonEvents.Server.passOnLevelTick(EventTiming.PRE, level);
                CommonEvents.Levels.passOnTick(EventTiming.PRE, level);
            });
        });
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
            event.getServer().getAllLevels().forEach(level -> {
                CommonEvents.Server.passOnLevelTick(EventTiming.POST, level);
                CommonEvents.Levels.passOnTick(EventTiming.POST, level);
            });
        });
        NeoForge.EVENT_BUS.addListener((LivingDeathEvent event) -> {
            CommonEvents.Entities.passOnDeath(event.getEntity(), event.getSource());
        });
        NeoForge.EVENT_BUS.addListener((LivingEquipmentChangeEvent event) -> {
            CommonEvents.Entities.passOnEquipmentChange(event.getEntity(), event.getSlot(), event.getFrom(), event.getTo());
        });

        NeoForge.EVENT_BUS.addListener((LevelEvent.Load event) -> {
            if (event.getLevel() instanceof ServerLevel level) CommonEvents.Server.passOnLevelLoad(level);
            if (event.getLevel() instanceof Level level && !level.isClientSide()) CommonEvents.Levels.passOnLoad(level);
        });
        NeoForge.EVENT_BUS.addListener((LevelEvent.Unload event) -> {
            if (event.getLevel() instanceof ServerLevel level) CommonEvents.Server.passOnLevelUnload(level);
            if (event.getLevel() instanceof Level level && !level.isClientSide()) CommonEvents.Levels.passOnUnload(level);
        });

        modEventBus.addListener(EventPriority.LOW, (BuildCreativeModeTabContentsEvent event) -> {
            NeoForgeCreativeEntryContext context = new NeoForgeCreativeEntryContext(event);
            CommonEvents.CreativeEntries.passModify(event.getTabKey(), context);
            context.apply();
        });
    }

    public static void modifyLootTable(ResourceKey<LootTable> key, LootTable originalTable, HolderLookup.Provider registries) {
        List<LootPool.Builder> pools = new ArrayList<>();
        for (LootPool pool : originalTable.pools) {
            pools.add(LootTableProvider.poolBuilder(pool));
        }

        boolean changed = CommonEvents.LootTables.passModify(key, new CommonEvents.LootTables.PoolAccess() {
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
        }, registries);

        if (!changed) {
            return;
        }
        originalTable.pools = pools.stream().map(LootPool.Builder::build).toList();
    }

    private static final class NeoForgeCreativeEntryContext implements CreativeEntryContext {
        private static final CreativeModeTab.TabVisibility VISIBILITY = CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;

        private final BuildCreativeModeTabContentsEvent event;
        private final List<ItemStack> inserted = new ArrayList<>();
        private final List<RelativeEntry> insertedAfter = new ArrayList<>();
        private final List<RelativeEntry> insertedBefore = new ArrayList<>();

        private NeoForgeCreativeEntryContext(BuildCreativeModeTabContentsEvent event) {
            this.event = event;
        }

        @Override
        public void insert(ItemLike... items) {
            for (ItemLike item : items) {
                inserted.add(item.asItem().getDefaultInstance());
            }
        }

        @Override
        public void insert(ItemStack... items) {
            for (ItemStack item : items) {
                inserted.add(item.copy());
            }
        }

        @Override
        public void insertAfter(ItemLike existingItem, ItemLike... addedItems) {
            for (ItemLike addedItem : addedItems) {
                insertedAfter.add(RelativeEntry.forItem(RelativePlacement.AFTER, existingItem, addedItem.asItem().getDefaultInstance()));
            }
        }

        @Override
        public void insertAfter(ItemLike existingItem, ItemStack... addedItems) {
            for (ItemStack addedItem : addedItems) {
                insertedAfter.add(RelativeEntry.forItem(RelativePlacement.AFTER, existingItem, addedItem));
            }
        }

        @Override
        public void insertAfter(ItemStack existingItem, ItemStack... addedItems) {
            for (ItemStack addedItem : addedItems) {
                insertedAfter.add(RelativeEntry.forStack(RelativePlacement.AFTER, existingItem, addedItem));
            }
        }

        @Override
        public void insertBefore(ItemLike existingItem, ItemLike... addedItems) {
            for (ItemLike addedItem : addedItems) {
                insertedBefore.add(RelativeEntry.forItem(RelativePlacement.BEFORE, existingItem, addedItem.asItem().getDefaultInstance()));
            }
        }

        @Override
        public void insertBefore(ItemLike existingItem, ItemStack... addedItems) {
            for (ItemStack addedItem : addedItems) {
                insertedBefore.add(RelativeEntry.forItem(RelativePlacement.BEFORE, existingItem, addedItem));
            }
        }

        @Override
        public void insertBefore(ItemStack existingItem, ItemStack... addedItems) {
            for (ItemStack addedItem : addedItems) {
                insertedBefore.add(RelativeEntry.forStack(RelativePlacement.BEFORE, existingItem, addedItem));
            }
        }

        private void apply() {
            for (ItemStack item : inserted) {
                event.accept(item, VISIBILITY);
            }

            List<RelativeEntry> pending = new ArrayList<>(insertedAfter.size() + insertedBefore.size());
            ListIterator<RelativeEntry> afterIterator = insertedAfter.listIterator(insertedAfter.size());
            while (afterIterator.hasPrevious()) {
                pending.add(afterIterator.previous());
            }
            pending.addAll(insertedBefore);

            boolean changed;
            do {
                changed = false;
                Iterator<RelativeEntry> iterator = pending.iterator();
                while (iterator.hasNext()) {
                    RelativeEntry entry = iterator.next();
                    ItemStack anchor = findAnchor(entry);
                    if (anchor == null) {
                        continue;
                    }

                    if (entry.placement == RelativePlacement.AFTER) {
                        event.insertAfter(anchor, entry.added, VISIBILITY);
                    } else {
                        event.insertBefore(anchor, entry.added, VISIBILITY);
                    }
                    iterator.remove();
                    changed = true;
                }
            } while (changed);

            for (RelativeEntry entry : pending) {
                LogUtils.getLogger().warn(
                        "Failed to add item {} {} anchor item {} in NeoForge creative tab {} because the anchor was not present",
                        BuiltInRegistries.ITEM.getKey(entry.added.getItem()),
                        entry.placement == RelativePlacement.AFTER ? "after" : "before",
                        BuiltInRegistries.ITEM.getKey(entry.anchor.getItem()),
                        event.getTabKey().identifier()
                );
            }
        }

        private @Nullable ItemStack findAnchor(RelativeEntry entry) {
            ItemStack anchor = findAnchorIn(event.getParentEntries(), entry);
            return anchor != null ? anchor : findAnchorIn(event.getSearchEntries(), entry);
        }

        private static @Nullable ItemStack findAnchorIn(Iterable<ItemStack> entries, RelativeEntry entry) {
            for (ItemStack stack : entries) {
                if (entry.matchComponents
                        ? ItemStack.isSameItemSameComponents(stack, entry.anchor)
                        : stack.is(entry.anchor.getItem())) {
                    return stack;
                }
            }
            return null;
        }
    }

    private enum RelativePlacement {
        BEFORE,
        AFTER
    }

    private static final class RelativeEntry {
        private final ItemStack anchor;
        private final ItemStack added;
        private final boolean matchComponents;
        private final RelativePlacement placement;

        private RelativeEntry(ItemStack anchor, ItemStack added, boolean matchComponents, RelativePlacement placement) {
            this.anchor = anchor;
            this.added = added;
            this.matchComponents = matchComponents;
            this.placement = placement;
        }

        private static RelativeEntry forItem(RelativePlacement placement, ItemLike anchor, ItemStack added) {
            return new RelativeEntry(anchor.asItem().getDefaultInstance(), added.copy(), false, placement);
        }

        private static RelativeEntry forStack(RelativePlacement placement, ItemStack anchor, ItemStack added) {
            return new RelativeEntry(anchor.copy(), added.copy(), true, placement);
        }
    }
}
