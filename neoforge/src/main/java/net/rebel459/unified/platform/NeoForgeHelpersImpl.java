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
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handlers.ServerPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.rebel459.unified.util.PackInfo;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NeoForgeHelpersImpl {

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

    public static class Networking implements HelpersImpl.Networking {

        @Override
        public void send(CustomPacketPayload payload, ServerPlayer player) {
            player.connection.send(new ClientboundCustomPayloadPacket(payload));
        }

        public static List<Pair<CustomPacketPayload.Type, StreamCodec>> C2S_LIST = new ArrayList<>();
        public static List<Pair<CustomPacketPayload.Type, StreamCodec>> S2C_LIST = new ArrayList<>();

        @Override
        public void registerC2S(CustomPacketPayload.Type type, StreamCodec codec) {
            C2S_LIST.add(Pair.of(type, codec));
        }

        @Override
        public void registerS2C(CustomPacketPayload.Type type, StreamCodec codec) {
            S2C_LIST.add(Pair.of(type, codec));
        }

        @SubscribeEvent
        public static void register(RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar("1");
            for (Pair<CustomPacketPayload.Type, StreamCodec> pair : C2S_LIST) {
                registrar.playToServer(
                        pair.getFirst(),
                        pair.getSecond(),
                        ServerPayloadHandler::handle
                );
            }
            for (Pair<CustomPacketPayload.Type, StreamCodec> pair : S2C_LIST) {
                registrar.playToClient(
                        pair.getFirst(),
                        pair.getSecond(),
                        ServerPayloadHandler::handle
                );
            }
        }

        private static final List<C2SRegistration<?>> C2S_REGS = new ArrayList<>();
        private static final List<S2CRegistration<?>> S2C_REGS = new ArrayList<>();

        private record C2SRegistration<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type, StreamCodec<FriendlyByteBuf, T> codec, BiConsumer<T, ServerPlayer> handler) {}

        private record S2CRegistration<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type, StreamCodec<FriendlyByteBuf, T> codec, Consumer<T> handler) {}

        @Override
        public void registerC2S(CustomPacketPayload.Type type, StreamCodec codec, BiConsumer handler) {

            C2S_REGS.add(new C2SRegistration<>(type, codec, handler));
        }

        @Override
        public void registerS2C(CustomPacketPayload.Type type, StreamCodec codec, Consumer handler) {

            S2C_REGS.add(new S2CRegistration<>(type, codec, handler));
        }

        @SubscribeEvent
        public static void registerWithHandler(RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar("1");

            for (C2SRegistration<?> reg : C2S_REGS) {
                @SuppressWarnings("unchecked")
                C2SRegistration r = reg;

                registrar.playToServer(
                        r.type,
                        r.codec,
                        (payload, context) -> {
                            r.handler.accept(payload, context.player());
                        }
                );
            }

            for (S2CRegistration<?> reg : S2C_REGS) {
                @SuppressWarnings("unchecked")
                S2CRegistration r = reg;

                registrar.playToClient(
                        r.type,
                        r.codec,
                        (payload, context) -> r.handler.accept(payload)
                );
            }
        }
    }

    public static class Platform implements HelpersImpl.Platform {

        @Override
        public net.rebel459.unified.util.Platform getPlatform() {
            return net.rebel459.unified.util.Platform.NEOFORGE;
        }

        @Override
        public boolean isModLoaded(String modId) {
            return ModList.get().isLoaded(modId);
        }

    }
}