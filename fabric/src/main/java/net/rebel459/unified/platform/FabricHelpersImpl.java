package net.rebel459.unified.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.registry.FuelValueEvents;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.fabric.impl.networking.payload.PayloadHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.rebel459.unified.util.EnvInfo;
import net.rebel459.unified.util.PackInfo;
import net.rebel459.unified.util.PlatformInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class FabricHelpersImpl {

    public static class Platform implements HelpersImpl.Platform {

        @Override
        public PlatformInfo getPlatform() {
            return PlatformInfo.FABRIC;
        }

        @Override
        public EnvInfo getEnvironment() {
            return switch (FabricLoader.getInstance().getEnvironmentType()) {
                case CLIENT -> EnvInfo.CLIENT;
                case SERVER -> EnvInfo.SERVER;
            };
        }

        @Override
        public boolean isModLoaded(String modId) {
            return FabricLoader.getInstance().isModLoaded(modId);
        }
    }

    public static class CreativeEntries implements HelpersImpl.CreativeEntries {

        @Override
        public final void insert(ResourceKey<CreativeModeTab> tab, ItemLike... items) {
            var itemList = Arrays.stream(items).toList();
            for (ItemLike itemLike : itemList) {
                insert(tab, itemLike.asItem().getDefaultInstance());
            }
        }

        @SafeVarargs
        @Override
        public final void insert(ResourceKey<CreativeModeTab> tab, ItemStack... items) {
            var itemList = Arrays.stream(items).toList();
            for (int x = itemList.size() - 1; x >= 0; x--) {
                ItemStack item = itemList.get(x);
                CreativeModeTabEvents.modifyOutputEvent(tab).register(entries -> {
                    entries.accept(item);
                });
            }
        }

        @Override
        public void insert(List<ResourceKey<CreativeModeTab>> tabs, ItemLike... items) {
            for (ResourceKey<CreativeModeTab> tab : tabs) {
                insert(tab, items);
            }
        }

        @Override
        public void insert(List<ResourceKey<CreativeModeTab>> tabs, ItemStack... items) {
            for (ResourceKey<CreativeModeTab> tab : tabs) {
                insert(tab, items);
            }
        }

        @Override
        public final void insertAfter(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemLike... addedItems) {
            var itemList = Arrays.stream(addedItems).toList();
            for (ItemLike itemLike : itemList) {
                insertAfter(tab, existingItem, itemLike.asItem().getDefaultInstance());
            }
        }

        @Override
        public void insertAfter(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemStack... addedItems) {
            CreativeModeTabEvents.modifyOutputEvent(tab).register(entries -> {
                entries.insertAfter(existingItem, addedItems);
            });
        }

        @Override
        public void insertAfter(List<ResourceKey<CreativeModeTab>> tabs, ItemLike existingItem, ItemLike... addedItems) {
            for (ResourceKey<CreativeModeTab> tab : tabs) {
                insertAfter(tab, existingItem, addedItems);
            }
        }

        @Override
        public void insertAfter(List<ResourceKey<CreativeModeTab>> tabs, ItemLike existingItem, ItemStack... addedItems) {
            for (ResourceKey<CreativeModeTab> tab : tabs) {
                insertAfter(tab, existingItem, addedItems);
            }
        }

        @Override
        public final void insertBefore(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemLike... addedItems) {
            var itemList = Arrays.stream(addedItems).toList();
            for (ItemLike itemLike : itemList) {
                insertBefore(tab, existingItem, itemLike.asItem().getDefaultInstance());
            }
        }

        @Override
        public void insertBefore(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemStack... addedItems) {
            CreativeModeTabEvents.modifyOutputEvent(tab).register(entries -> {
                entries.insertBefore(existingItem, addedItems);
            });
        }

        @Override
        public void insertBefore(List<ResourceKey<CreativeModeTab>> tabs, ItemLike existingItem, ItemLike... addedItems) {
            for (ResourceKey<CreativeModeTab> tab : tabs) {
                insertBefore(tab, existingItem, addedItems);
            }
        }

        @Override
        public void insertBefore(List<ResourceKey<CreativeModeTab>> tabs, ItemLike existingItem, ItemStack... addedItems) {
            for (ResourceKey<CreativeModeTab> tab : tabs) {
                insertBefore(tab, existingItem, addedItems);
            }
        }
    }

    public static class Packs implements HelpersImpl.Packs {

        @Override
        public void add(Identifier id, PackInfo info) {
            if (FabricLoader.getInstance().getModContainer(id.getNamespace()).isEmpty()) return;
            ResourceLoader.registerBuiltinPack(
                    id, FabricLoader.getInstance().getModContainer(id.getNamespace()).get(),
                    Component.translatable("pack." + id.getNamespace() + "." + id.getPath()),
                    getActivationType(info)
            );
        }

        public static PackActivationType getActivationType(PackInfo info) {
            return switch (info) {
                case REQUIRED_DATA, REQUIRED_RESOURCES -> PackActivationType.ALWAYS_ENABLED;
                case OPTIONAL_DATA, OPTIONAL_RESOURCES -> PackActivationType.DEFAULT_ENABLED;
            };
        }
    }

    public static class LootTables implements HelpersImpl.LootTables {

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

    public static class Networking implements HelpersImpl.Networking {

        @Override
        public void registerPlayToServer(CustomPacketPayload.Type type, StreamCodec codec) {
            PayloadTypeRegistry.serverboundPlay().register(type, codec);
        }

        @Override
        public void registerPlayToClient(CustomPacketPayload.Type type, StreamCodec codec) {
            PayloadTypeRegistry.clientboundPlay().register(type, codec);
        }

        @Override
        public void registerPlayToServer(CustomPacketPayload.Type type, StreamCodec codec, BiConsumer handler) {

            PayloadTypeRegistry.serverboundPlay().register(type, codec);

            ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
                handler.accept(payload, context.player());
            });
        }

        @Override
        public void registerPlayToClient(CustomPacketPayload.Type type, StreamCodec codec, BiConsumer handler) {

            PayloadTypeRegistry.clientboundPlay().register(type, codec);

            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
                    handler.accept(payload, context.player());
                });
            }
        }

        @Override
        public void registerConfigToServer(CustomPacketPayload.Type type, StreamCodec codec) {
            PayloadTypeRegistry.serverboundConfiguration().register(type, codec);
        }

        @Override
        public void registerConfigToClient(CustomPacketPayload.Type type, StreamCodec codec) {
            PayloadTypeRegistry.clientboundConfiguration().register(type, codec);
        }

        @Override
        public void registerConfigToServer(CustomPacketPayload.Type type, StreamCodec codec, BiConsumer handler) {

            PayloadTypeRegistry.serverboundConfiguration().register(type, codec);

            ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
                handler.accept(payload, context.player());
            });
        }

        @Override
        public void registerConfigToClient(CustomPacketPayload.Type type, StreamCodec codec, BiConsumer handler) {

            PayloadTypeRegistry.clientboundConfiguration().register(type, codec);

            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
                    handler.accept(payload, context.player());
                });
            }
        }

        @Override
        public void send(CustomPacketPayload payload, ServerPlayer player) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}