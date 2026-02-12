package net.rebel459.unified.platform;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.rebel459.unified.test.UnifiedTest;
import net.rebel459.unified.util.PackInfo;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class NeoForgeUnifiedEvents {

    public static void init() {
        UnifiedFactory.setEvents(new UnifiedFactory.Events() {
            @Override
            public UnifiedEvents.CreativeEntries createCreativeEntries() {
                return new CreativeEntries();
            }

            @Override
            public UnifiedEvents.LootTables createLootTables() {
                return new LootTables();
            }

            @Override
            public UnifiedEvents.Packs createPacks() {
                return new Packs();
            }

            @Override
            public UnifiedEvents.FurnaceFuels createFurnaceFuels() {
                return new FurnaceFuel();
            }

            @Override
            public UnifiedEvents.StrippableBlocks createStrippableBlocks() {
                return new StrippableBlocks();
            }
        });
    }

    public static class FurnaceFuel implements UnifiedEvents.FurnaceFuels {

        private static final Object2IntMap<ItemLike> ITEMS = new Object2IntLinkedOpenHashMap<>();

        static {
            NeoForge.EVENT_BUS.register(FurnaceFuel.class);
        }

        @Override
        public void add(ItemLike item, int ticks) {
            ITEMS.put(item, ticks);
        }

        static {
            NeoForge.EVENT_BUS.register(FurnaceFuel.class);
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

    public static class CreativeEntries implements UnifiedEvents.CreativeEntries {

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

    public static class Packs implements UnifiedEvents.Packs {

        public static List<Pair<Identifier, PackInfo>> PACK_LIST = new ArrayList<>();

        @Override
        public void add(Identifier id, PackInfo info) {
            PACK_LIST.add(Pair.of(id, info));
        }

        public static boolean getBoolean(PackInfo info) {
            return switch (info) {
                case REQUIRED_DATA, REQUIRED_RESOURCES -> true;
                case OPTIONAL_DATA, OPTIONAL_RESOURCES -> false;
            };
        }

        public static PackType getType(PackInfo info) {
            return switch (info) {
                case REQUIRED_DATA, OPTIONAL_DATA -> PackType.SERVER_DATA;
                case REQUIRED_RESOURCES, OPTIONAL_RESOURCES -> PackType.CLIENT_RESOURCES;
            };
        }

        @SubscribeEvent
        public static void addFeaturePacks(AddPackFindersEvent event) {
            for (Pair<Identifier, PackInfo> pair : PACK_LIST) {
                Identifier id = pair.getFirst();
                PackInfo info = pair.getSecond();

                event.addPackFinders(
                        Identifier.fromNamespaceAndPath(id.getNamespace(), "resourcepacks/" + id.getPath()),
                        getType(info),
                        Component.translatable("pack." + id.getNamespace() + "." + id.getPath()),
                        PackSource.BUILT_IN,
                        getBoolean(info),
                        Pack.Position.TOP
                );
            }
        }
    }

    public static class LootTables implements UnifiedEvents.LootTables {

        public static List<Pair<LootPool.Builder, ResourceKey<LootTable>>> LOOT_APPENDER_LIST = new ArrayList<>();

        static {
            NeoForge.EVENT_BUS.register(LootTables.class);
        }

        @Override
        public void addPool(ResourceKey<LootTable> table, LootPool.Builder... pools) {
            var poolList = Arrays.stream(pools).toList();
            for (LootPool.Builder pool : poolList) {
                addPool(List.of(table), pool);
            }
        }

        @Override
        public final void addPool(List<ResourceKey<LootTable>> tables, LootPool.Builder... pools) {
            var poolList = Arrays.stream(pools).toList();
            for (LootPool.Builder pool : poolList) {
                for (ResourceKey<LootTable> table : tables) {
                    LOOT_APPENDER_LIST.add(Pair.of(pool, table));
                }
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

    public static class StrippableBlocks implements UnifiedEvents.StrippableBlocks {

        public static HashMap<Block, Block> STRIPPABLES = new HashMap<>(AxeItem.STRIPPABLES);

        @Override
        public void add(Block original, Block stripped) {
            STRIPPABLES.put(original, stripped);
        }

        @SubscribeEvent
        public static void strippables(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                AxeItem.STRIPPABLES = STRIPPABLES;
            });
        }
    }
}