package net.rebel459.unified.platform;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handlers.ServerPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.rebel459.unified.util.EnvInfo;
import net.rebel459.unified.util.PackInfo;
import net.rebel459.unified.util.PlatformInfo;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;

public class NeoForgeHelpersImpl {

    public static class Platform implements HelpersImpl.Platform {

        @Override
        public PlatformInfo getPlatform() {
            return PlatformInfo.NEOFORGE;
        }

        @Override
        public EnvInfo getEnvironment() {
            return switch (FMLEnvironment.getDist()) {
                case CLIENT -> EnvInfo.CLIENT;
                case DEDICATED_SERVER -> EnvInfo.SERVER;
            };
        }

        @Override
        public boolean isModLoaded(String modId) {
            return ModList.get().isLoaded(modId);
        }
    }

    public static class FurnaceFuels implements HelpersImpl.FurnaceFuels {

        private static final Object2IntMap<ItemLike> ITEMS = new Object2IntLinkedOpenHashMap<>();

        static {
            NeoForge.EVENT_BUS.register(FurnaceFuels.class);
        }

        @Override
        public void add(ItemLike item, int ticks) {
            ITEMS.put(item, ticks);
        }

        static {
            NeoForge.EVENT_BUS.register(FurnaceFuels.class);
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

    public static class CreativeEntries implements HelpersImpl.CreativeEntries {

        private static List<Pair<ItemStack, ResourceKey<CreativeModeTab>>> INSERT_ITEMS = new ArrayList<>();
        private static List<Triple<ItemLike, ItemStack, ResourceKey<CreativeModeTab>>> INSERT_AFTER_ITEMS = new ArrayList<>();
        private static List<Triple<ItemLike, ItemStack, ResourceKey<CreativeModeTab>>> INSERT_BEFORE_ITEMS = new ArrayList<>();

        @Override
        public final void insert(ResourceKey<CreativeModeTab> tab, ItemLike... items) {
            var itemList = Arrays.stream(items).toList();
            for (ItemLike itemLike : itemList) {
                insert(tab, itemLike.asItem().getDefaultInstance());
            }
        }

        @Override
        public void insert(ResourceKey<CreativeModeTab> tab, ItemStack... items) {
            List<ItemStack> itemList = Arrays.stream(items).toList();
            for (ItemStack item : itemList) {
                INSERT_ITEMS.add(Pair.of(item, tab));
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
            List<ItemStack> itemList = Arrays.stream(addedItems).toList();
            for (ItemStack addedItem : itemList) {
                INSERT_AFTER_ITEMS.add(Triple.of(existingItem, addedItem, tab));
            }
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
            List<ItemStack> itemList = Arrays.stream(addedItems).toList();
            for (ItemStack addedItem : itemList) {
                INSERT_BEFORE_ITEMS.add(Triple.of(existingItem, addedItem, tab));
            }
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

        @SubscribeEvent
        public static void buildContents(BuildCreativeModeTabContentsEvent event) {
            for (Pair<ItemStack, ResourceKey<CreativeModeTab>> pair : INSERT_ITEMS) {
                ItemStack item = pair.getFirst();
                ResourceKey<CreativeModeTab> tab = pair.getSecond();
                if (event.getTabKey().equals(tab)) {
                    event.accept(item);
                }
            }
            for (int x = INSERT_AFTER_ITEMS.size() - 1; x >= 0; x--) {
                var triple = INSERT_AFTER_ITEMS.get(x);
                ItemLike existingItem = triple.getLeft();
                ItemStack addedItem = triple.getMiddle();
                ResourceKey<CreativeModeTab> tab = triple.getRight();
                if (event.getTabKey().equals(tab)) {
                    event.insertAfter(existingItem.asItem().getDefaultInstance(), addedItem, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                }
            }
            for (int x = INSERT_BEFORE_ITEMS.size() - 1; x >= 0; x--) {
                var triple = INSERT_BEFORE_ITEMS.get(x);
                ItemLike existingItem = triple.getLeft();
                ItemStack addedItem = triple.getMiddle();
                ResourceKey<CreativeModeTab> tab = triple.getRight();
                if (event.getTabKey().equals(tab)) {
                    event.insertBefore(existingItem.asItem().getDefaultInstance(), addedItem, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                }
            }
        }
    }

    public static class Packs implements HelpersImpl.Packs {

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

    public static class LootTables implements HelpersImpl.LootTables {

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

    public static class StrippableBlocks implements HelpersImpl.StrippableBlocks {

        public static HashMap<Block, Block> STRIPPABLES = new HashMap<>();

        @Override
        public void add(Block original, Block stripped) {
            STRIPPABLES.put(original, stripped);
        }

        @SubscribeEvent
        public static void strippables(BlockEvent.BlockToolModificationEvent event) {
            if (event.getItemAbility() != ItemAbilities.AXE_STRIP) return;

            BlockState originalState = event.getState();
            Block originalBlock = originalState.getBlock();

            Block strippedBlock = STRIPPABLES.get(originalBlock);
            if (strippedBlock == null) return;

            BlockState strippedState = strippedBlock.defaultBlockState();

            for (Property property : originalState.getProperties()) {
                if (strippedState.hasProperty(property)) {
                    strippedState = strippedState.setValue(property, originalState.getValue(property));
                }
            }

            event.setFinalState(strippedState);
        }
    }

    public static class Networking implements HelpersImpl.Networking {

        @Override
        public void send(CustomPacketPayload payload, ServerPlayer player) {
            player.connection.send(new ClientboundCustomPayloadPacket(payload));
        }

        private static final List<ToServer> TO_SERVER_LIST = new ArrayList<>();
        private static final List<ToClient> TO_CLIENT_LIST = new ArrayList<>();

        private record ToServer<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type, StreamCodec<FriendlyByteBuf, T> codec, boolean play) {}
        private record ToClient<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type, StreamCodec<FriendlyByteBuf, T> codec, boolean play) {}

        @Override
        public void registerPlayToServer(CustomPacketPayload.Type type, StreamCodec codec) {
            TO_SERVER_LIST.add(new ToServer<CustomPacketPayload>(type, codec, true));
        }

        @Override
        public void registerPlayToClient(CustomPacketPayload.Type type, StreamCodec codec) {
            TO_CLIENT_LIST.add(new ToClient<CustomPacketPayload>(type, codec, true));
        }

        @Override
        public void registerConfigToServer(CustomPacketPayload.Type type, StreamCodec codec) {
            TO_SERVER_LIST.add(new ToServer<CustomPacketPayload>(type, codec, false));
        }

        @Override
        public void registerConfigToClient(CustomPacketPayload.Type type, StreamCodec codec) {
            TO_CLIENT_LIST.add(new ToClient<CustomPacketPayload>(type, codec, false));
        }

        @SubscribeEvent
        public static void register(RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar("1");
            for (ToServer entry : TO_SERVER_LIST) {
                if (entry.play) {
                    registrar.playToServer(
                            entry.type,
                            entry.codec,
                            ServerPayloadHandler::handle
                    );
                } else {
                    registrar.configurationToServer(
                            entry.type,
                            entry.codec,
                            ServerPayloadHandler::handle
                    );
                }
            }
            for (ToClient entry : TO_CLIENT_LIST) {
                if (entry.play) {
                    registrar.playToClient(
                            entry.type,
                            entry.codec
                    );
                } else {
                    registrar.configurationToClient(
                            entry.type,
                            entry.codec
                    );
                }
            }
        }

        private static final List<HandledToServer<?>> HANDLED_TO_SERVER_LIST = new ArrayList<>();
        private static final List<HandledToClient<?>> HANDLED_TO_CLIENT_LIST = new ArrayList<>();

        private record HandledToServer<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type, StreamCodec<FriendlyByteBuf, T> codec, BiConsumer<T, ServerPlayer> handler, boolean play) {}
        private record HandledToClient<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type, StreamCodec<FriendlyByteBuf, T> codec, BiConsumer<T, Player> handler, boolean play) {}

        @Override
        public void registerPlayToServer(CustomPacketPayload.Type type, StreamCodec codec, BiConsumer handler) {
            HANDLED_TO_SERVER_LIST.add(new HandledToServer<>(type, codec, handler, true));
        }

        @Override
        public void registerPlayToClient(CustomPacketPayload.Type type, StreamCodec codec, BiConsumer handler) {
            HANDLED_TO_CLIENT_LIST.add(new HandledToClient<>(type, codec, handler, true));
        }

        @Override
        public void registerConfigToServer(CustomPacketPayload.Type type, StreamCodec codec, BiConsumer handler) {
            HANDLED_TO_SERVER_LIST.add(new HandledToServer<>(type, codec, handler, false));
        }

        @Override
        public void registerConfigToClient(CustomPacketPayload.Type type, StreamCodec codec, BiConsumer handler) {
            HANDLED_TO_CLIENT_LIST.add(new HandledToClient<>(type, codec, handler, false));
        }

        @SubscribeEvent
        public static void registerWithHandler(RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar("1");

            for (HandledToServer handled : HANDLED_TO_SERVER_LIST) {

                if (handled.play) {
                    registrar.playToServer(
                            handled.type,
                            handled.codec,
                            (payload, context) -> {
                                handled.handler.accept(payload, context.player());
                            }
                    );
                } else {
                    registrar.configurationToServer(
                            handled.type,
                            handled.codec,
                            (payload, context) -> {
                                handled.handler.accept(payload, context.player());
                            }
                    );
                }
            }

            for (HandledToClient handled : HANDLED_TO_CLIENT_LIST) {

                if (handled.play) {
                    registrar.playToClient(
                            handled.type,
                            handled.codec,
                            (payload, context) -> handled.handler.accept(payload, context.player())
                    );
                } else {
                    registrar.configurationToClient(
                            handled.type,
                            handled.codec,
                            (payload, context) -> handled.handler.accept(payload, context.player())
                    );
                }
            }
        }
    }
}