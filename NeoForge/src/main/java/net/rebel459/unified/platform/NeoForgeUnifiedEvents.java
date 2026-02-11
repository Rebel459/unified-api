package net.rebel459.unified.platform;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.rebel459.unified.util.PackInfo;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class NeoForgeUnifiedEvents {

    public static void init() {
        UnifiedFactory.setEvents(new UnifiedFactory.Events() {
            @Override
            public UnifiedEvents.CreativeEvent createCreativeEvent() {
                return new NeoForgeUnifiedEvents.CreativeEvent();
            }

            @Override
            public UnifiedEvents.LootEvent createLootEvent() {
                return new NeoForgeUnifiedEvents.LootEvent();
            }

            @Override
            public UnifiedEvents.PackEvent createPackEvent() {
                return new NeoForgeUnifiedEvents.PackEvent();
            }

            @Override
            public UnifiedEvents.FuelEvent createFuelEvent() {
                return new NeoForgeUnifiedEvents.FuelEvent();
            }
        });
    }

    public static class FuelEvent implements UnifiedEvents.FuelEvent {

        private static final Object2IntMap<ItemLike> ITEMS = new Object2IntLinkedOpenHashMap<>();

        static {
            NeoForge.EVENT_BUS.register(FuelEvent.class);
        }

        @Override
        public void add(ItemLike item, int ticks) {
            ITEMS.put(item, ticks);
        }

        @Override
        public void add(TagKey<Item> tag, int ticks) {
            List<Holder<Item>> list = VanillaRegistries.createLookup().lookupOrThrow(net.minecraft.core.registries.Registries.ITEM).get(tag).map(HolderSet.Named::stream).orElse(Stream.empty()).toList();
            list.forEach(itemHolder -> ITEMS.put(itemHolder.value(), ticks));
        }

        static {
            NeoForge.EVENT_BUS.register(FuelEvent.class);
        }

        @SubscribeEvent
        public static void event(FurnaceFuelBurnTimeEvent event) {
            if (event.getItemStack().isEmpty()) return;
            int time = ITEMS.getOrDefault(event.getItemStack().getItem(), Integer.MIN_VALUE);
            if (time != Integer.MIN_VALUE) {
                event.setBurnTime(time);
            }
        }
    }

    public static class CreativeEvent implements UnifiedEvents.CreativeEvent {

        private static List<Pair<ItemStack, ResourceKey<CreativeModeTab>>> ADD_ITEMS = new ArrayList<>();
        private static List<Triple<ItemLike, ItemStack, ResourceKey<CreativeModeTab>>> ADD_AFTER_ITEMS = new ArrayList<>();
        private static List<Triple<ItemLike, ItemStack, ResourceKey<CreativeModeTab>>> ADD_BEFORE_ITEMS = new ArrayList<>();

        @Override
        public final void add(ResourceKey<CreativeModeTab> tab, ItemLike... items) {
            var itemList = Arrays.stream(items).toList();
            for (ItemLike itemLike : itemList) {
                add(tab, itemLike.asItem().getDefaultInstance());
            }
        }

        @Override
        public void add(ResourceKey<CreativeModeTab> tab, ItemStack... items) {
            List<ItemStack> itemList = Arrays.stream(items).toList();
            for (ItemStack item : itemList) {
                ADD_ITEMS.add(Pair.of(item, tab));
            }
        }

        @Override
        public final void addAfter(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemLike... addedItems) {
            var itemList = Arrays.stream(addedItems).toList();
            for (ItemLike itemLike : itemList) {
                addAfter(tab, existingItem, itemLike.asItem().getDefaultInstance());
            }
        }

        @Override
        public void addAfter(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemStack... addedItems) {
            List<ItemStack> itemList = Arrays.stream(addedItems).toList();
            for (ItemStack addedItem : itemList) {
                ADD_AFTER_ITEMS.add(Triple.of(existingItem, addedItem, tab));
            }
        }

        @Override
        public final void addBefore(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemLike... addedItems) {
            var itemList = Arrays.stream(addedItems).toList();
            for (ItemLike itemLike : itemList) {
                addBefore(tab, existingItem, itemLike.asItem().getDefaultInstance());
            }
        }

        @Override
        public void addBefore(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemStack... addedItems) {
            List<ItemStack> itemList = Arrays.stream(addedItems).toList();
            for (ItemStack addedItem : itemList) {
                ADD_BEFORE_ITEMS.add(Triple.of(existingItem, addedItem, tab));
            }
        }

        @SubscribeEvent
        public static void buildContents(BuildCreativeModeTabContentsEvent event) {
            for (Pair<ItemStack, ResourceKey<CreativeModeTab>> pair : ADD_ITEMS) {
                ItemStack item = pair.getFirst();
                ResourceKey<CreativeModeTab> tab = pair.getSecond();
                if (event.getTabKey().equals(tab)) {
                    event.accept(item);
                }
            }
            for (int x = ADD_AFTER_ITEMS.size() - 1; x >= 0; x--) {
                var triple = ADD_AFTER_ITEMS.get(x);
                ItemLike existingItem = triple.getLeft();
                ItemStack addedItem = triple.getMiddle();
                ResourceKey<CreativeModeTab> tab = triple.getRight();
                if (event.getTabKey().equals(tab)) {
                    event.insertAfter(existingItem.asItem().getDefaultInstance(), addedItem, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                }
            }
            for (int x = ADD_BEFORE_ITEMS.size() - 1; x >= 0; x--) {
                var triple = ADD_BEFORE_ITEMS.get(x);
                ItemLike existingItem = triple.getLeft();
                ItemStack addedItem = triple.getMiddle();
                ResourceKey<CreativeModeTab> tab = triple.getRight();
                if (event.getTabKey().equals(tab)) {
                    event.insertBefore(existingItem.asItem().getDefaultInstance(), addedItem, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                }
            }
        }
    }

    public static class PackEvent implements UnifiedEvents.PackEvent {

        public static List<Pair<Identifier, PackInfo>> PACK_LIST = new ArrayList<>();

        @Override
        public void add(Identifier id, PackInfo info) {
            PACK_LIST.add(Pair.of(id, info));
        }

        @SubscribeEvent
        public static void addFeaturePacks(AddPackFindersEvent event) {
            for (Pair<Identifier, PackInfo> pair : PACK_LIST) {
                Identifier id = pair.getFirst();
                PackInfo info = pair.getSecond();

                boolean alwaysActive = false;
                PackType type = PackType.CLIENT_RESOURCES;
                if (info.equals(PackInfo.REQUIRED_RESOURCES)) {
                    alwaysActive = true;
                } else if (info.equals(PackInfo.OPTIONAL_DATA)) {
                    type = PackType.SERVER_DATA;
                } else if (info.equals(PackInfo.REQUIRED_DATA)) {
                    alwaysActive = true;
                    type = PackType.SERVER_DATA;
                }

                event.addPackFinders(
                        Identifier.fromNamespaceAndPath(id.getNamespace(), "resourcepacks/" + id.getPath()),
                        type,
                        Component.translatable("pack." + id.getNamespace() + "." + id.getPath()),
                        PackSource.BUILT_IN,
                        alwaysActive,
                        Pack.Position.TOP
                );
            }
        }
    }

    public static class LootEvent implements UnifiedEvents.LootEvent {

        public static List<Pair<LootPool.Builder, ResourceKey<LootTable>>> LOOT_APPENDER_LIST = new ArrayList<>();

        static {
            NeoForge.EVENT_BUS.register(LootEvent.class);
        }

        @Override
        public void addPool(ResourceKey<LootTable> table, LootPool.Builder pool) {
            addPool(List.of(table), pool);
        }

        @Override
        public final void addPool(List<ResourceKey<LootTable>> tables, LootPool.Builder pool) {
            for (ResourceKey<LootTable> table : tables) {
                LOOT_APPENDER_LIST.add(Pair.of(pool, table));
            }
        }

        @Override
        public void addItem(ResourceKey<LootTable> table, ItemLike item, int chance) {
            addItem(List.of(table), item, chance);
        }

        @Override
        public final void addItem(List<ResourceKey<LootTable>> tables, ItemLike item, int chance) {
            chance = Math.max(Math.min(chance, 100), 0);
            int emptyChance = 100 - chance;
            if (chance > 0 && chance < 100) {
                addPool(
                        tables,
                        LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                                .add(EmptyLootItem.emptyItem().setWeight(emptyChance))
                                .add(LootItem.lootTableItem(item).setWeight(chance))
                );
            }
            else if (chance == 100) {
                addPool(
                        tables,
                        LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                                .add(LootItem.lootTableItem(item))
                );
            }
        }

        @SubscribeEvent
        public static void onLootTableModify(LootTableLoadEvent event) {
            for (Pair<LootPool.Builder, ResourceKey<LootTable>> pair : LOOT_APPENDER_LIST) {
                if (event.getKey().equals(pair.getSecond())) {
                    event.getTable().addPool(pair.getFirst().build());
                }
            }
        }
    }
}