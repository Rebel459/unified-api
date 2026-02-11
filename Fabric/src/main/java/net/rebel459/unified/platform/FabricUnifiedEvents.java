package net.rebel459.unified.platform;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityType;
import net.fabricmc.fabric.api.registry.FuelRegistryEvents;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.rebel459.unified.util.PackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class FabricUnifiedEvents {

    public static void init() {
        UnifiedFactory.setEvents(new UnifiedFactory.Events() {
            @Override
            public UnifiedEvents.CreativeEvent createCreativeEvent() {
                return new FabricUnifiedEvents.CreativeEvent();
            }

            @Override
            public UnifiedEvents.LootEvent createLootEvent() {
                return new FabricUnifiedEvents.LootEvent();
            }

            @Override
            public UnifiedEvents.PackEvent createPackEvent() {
                return new FabricUnifiedEvents.PackEvent();
            }

            @Override
            public UnifiedEvents.FuelEvent createFuelEvent() {
                return new FabricUnifiedEvents.FuelEvent();
            }

            @Override
            public UnifiedEvents.StrippableEvent createStrippableEvent() {
                return new FabricUnifiedEvents.StrippableEvent();
            }
        });
    }

    public static class FuelEvent implements UnifiedEvents.FuelEvent {

        private static final List<FuelRegistryEvents.BuildCallback> CALLBACKS = new ArrayList<>();
        private static final List<FuelRegistryEvents.ExclusionsCallback> EXCLUSIONS_CALLBACKS = new ArrayList<>();

        @Override
        public void add(ItemLike item, int ticks) {
            CALLBACKS.add((builder, context) -> {
                if (ticks >= 0) {
                    builder.add(item, ticks);
                }
            });
            EXCLUSIONS_CALLBACKS.add((builder, context) -> {
                if (ticks < 0) {
                    builder.values.remove(item.asItem());
                }
            });
        }

        static {
            FuelRegistryEvents.BUILD.register((builder, context) -> {
                for (var callback : CALLBACKS) {
                    callback.build(builder, context);
                }
            });
            FuelRegistryEvents.EXCLUSIONS.register((builder, context) -> {
                for (var callback : EXCLUSIONS_CALLBACKS) {
                    callback.buildExclusions(builder, context);
                }
            });
        }
    }

    public static class CreativeEvent implements UnifiedEvents.CreativeEvent {

        @Override
        public final void add(ResourceKey<CreativeModeTab> tab, ItemLike... items) {
            var itemList = Arrays.stream(items).toList();
            for (ItemLike itemLike : itemList) {
                add(tab, itemLike.asItem().getDefaultInstance());
            }
        }

        @SafeVarargs
        @Override
        public final void add(ResourceKey<CreativeModeTab> tab, ItemStack... items) {
            var itemList = Arrays.stream(items).toList();
            for (int x = itemList.size() - 1; x >= 0; x--) {
                ItemStack item = itemList.get(x);
                ItemGroupEvents.modifyEntriesEvent(tab).register(entries -> {
                    entries.accept(item);
                });
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
            ItemGroupEvents.modifyEntriesEvent(tab).register(entries -> {
                entries.addAfter(existingItem, addedItems);
            });
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
            ItemGroupEvents.modifyEntriesEvent(tab).register(entries -> {
                entries.addBefore(existingItem, addedItems);
            });
        }
    }

    public static class PackEvent implements UnifiedEvents.PackEvent {

        @Override
        public void add(Identifier id, PackInfo info) {
            if (FabricLoader.getInstance().getModContainer(id.getNamespace()).isEmpty()) return;
            PackActivationType activationType = PackActivationType.DEFAULT_ENABLED;
            if (info.equals(PackInfo.REQUIRED_DATA) || info.equals(PackInfo.REQUIRED_RESOURCES)) activationType = PackActivationType.ALWAYS_ENABLED;
            ResourceLoader.registerBuiltinPack(
                    id, FabricLoader.getInstance().getModContainer(id.getNamespace()).get(),
                    Component.translatable("pack." + id.getNamespace() + "." + id.getPath()),
                    activationType
            );
        }
    }

    public static class LootEvent implements UnifiedEvents.LootEvent {

        @Override
        public void addPool(ResourceKey<LootTable> table, LootPool.Builder... pools) {
            var poolList = Arrays.stream(pools).toList();
            for (LootPool.Builder pool : poolList) {
                addPool(List.of(table), pool);
            }
        }

        @Override
        public final void addPool(List<ResourceKey<LootTable>> tables, LootPool.Builder... pools) {
            LootTableEvents.MODIFY.register((targetTable, tableBuilder, source, registries) -> {
                var poolList = Arrays.stream(pools).toList();
                for (LootPool.Builder pool : poolList) {
                    for (ResourceKey<LootTable> table : tables) {
                        if (targetTable.equals(table)) {
                            tableBuilder.withPool(pool);
                        }
                    }
                }
            });
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
            } else if (chance == 100) {
                addPool(
                        tables,
                        LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                                .add(LootItem.lootTableItem(item))
                );
            }
        }
    }

    public static class StrippableEvent implements UnifiedEvents.StrippableEvent {

        @Override
        public void add(Block original, Block stripped) {
            StrippableBlockRegistry.register(original, stripped);
        }
    }
}