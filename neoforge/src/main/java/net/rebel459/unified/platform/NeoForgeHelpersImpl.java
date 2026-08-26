package net.rebel459.unified.platform;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handlers.ServerPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.rebel459.unified.util.PackType;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NeoForgeHelpersImpl {

    public static class CreativeEntries implements HelpersImpl.CreativeEntries {

        private static List<Pair<ItemStackTemplate, ResourceKey<CreativeModeTab>>> INSERT_ITEMS = new ArrayList<>();
        private static List<Triple<ItemLike, ItemStackTemplate, ResourceKey<CreativeModeTab>>> INSERT_AFTER_ITEMS = new ArrayList<>();
        private static List<Triple<ItemLike, ItemStackTemplate, ResourceKey<CreativeModeTab>>> INSERT_BEFORE_ITEMS = new ArrayList<>();

        @Override
        public final void insert(ResourceKey<CreativeModeTab> tab, ItemLike... items) {
            var itemList = Arrays.stream(items).toList();
            for (ItemLike itemLike : itemList) {
                insert(tab, new ItemStackTemplate(itemLike.asItem()));
            }
        }

        @Override
        public void insert(ResourceKey<CreativeModeTab> tab, ItemStackTemplate... items) {
            List<ItemStackTemplate> itemList = Arrays.stream(items).toList();
            for (ItemStackTemplate item : itemList) {
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
        public void insert(List<ResourceKey<CreativeModeTab>> tabs, ItemStackTemplate... items) {
            for (ResourceKey<CreativeModeTab> tab : tabs) {
                insert(tab, items);
            }
        }

        @Override
        public final void insertAfter(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemLike... addedItems) {
            var itemList = Arrays.stream(addedItems).toList();
            for (ItemLike itemLike : itemList) {
                insertAfter(tab, existingItem, new ItemStackTemplate(itemLike.asItem()));
            }
        }

        @Override
        public void insertAfter(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemStackTemplate... addedItems) {
            List<ItemStackTemplate> itemList = Arrays.stream(addedItems).toList();
            for (ItemStackTemplate addedItem : itemList) {
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
        public void insertAfter(List<ResourceKey<CreativeModeTab>> tabs, ItemLike existingItem, ItemStackTemplate... addedItems) {
            for (ResourceKey<CreativeModeTab> tab : tabs) {
                insertAfter(tab, existingItem, addedItems);
            }
        }

        @Override
        public final void insertBefore(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemLike... addedItems) {
            var itemList = Arrays.stream(addedItems).toList();
            for (ItemLike itemLike : itemList) {
                insertBefore(tab, existingItem, new ItemStackTemplate(itemLike.asItem()));
            }
        }

        @Override
        public void insertBefore(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemStackTemplate... addedItems) {
            List<ItemStackTemplate> itemList = Arrays.stream(addedItems).toList();
            for (ItemStackTemplate addedItem : itemList) {
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
        public void insertBefore(List<ResourceKey<CreativeModeTab>> tabs, ItemLike existingItem, ItemStackTemplate... addedItems) {
            for (ResourceKey<CreativeModeTab> tab : tabs) {
                insertBefore(tab, existingItem, addedItems);
            }
        }

        @SubscribeEvent(priority = EventPriority.LOW)
        public static void buildContents(BuildCreativeModeTabContentsEvent event) {
            CreativeModeTab.TabVisibility visibility =
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;

            for (Pair<ItemStackTemplate, ResourceKey<CreativeModeTab>> pair : INSERT_ITEMS) {
                ItemStack item = pair.getFirst().create();
                ResourceKey<CreativeModeTab> tab = pair.getSecond();

                if (event.getTabKey().equals(tab)) {
                    event.accept(item, visibility);
                }
            }

            applyRelativeItems(event, visibility);
        }

        private static void applyRelativeItems(BuildCreativeModeTabContentsEvent event, CreativeModeTab.TabVisibility visibility) {
            List<RelativeEntry> pending = new ArrayList<>();

            ListIterator<Triple<ItemLike, ItemStackTemplate, ResourceKey<CreativeModeTab>>> afterIterator = INSERT_AFTER_ITEMS.listIterator(INSERT_AFTER_ITEMS.size());
            while (afterIterator.hasPrevious()) {
                Triple<ItemLike, ItemStackTemplate, ResourceKey<CreativeModeTab>> triple = afterIterator.previous();
                if (event.getTabKey().equals(triple.getRight())) {
                    pending.add(new RelativeEntry(RelativePlacement.AFTER, triple.getLeft(), triple.getMiddle(), triple.getRight()));
                }
            }

            for (Triple<ItemLike, ItemStackTemplate, ResourceKey<CreativeModeTab>> triple : INSERT_BEFORE_ITEMS) {
                if (event.getTabKey().equals(triple.getRight())) {
                    pending.add(new RelativeEntry(RelativePlacement.BEFORE, triple.getLeft(), triple.getMiddle(), triple.getRight()));
                }
            }

            boolean changed;

            do {
                changed = false;

                Iterator<RelativeEntry> iterator = pending.iterator();

                while (iterator.hasNext()) {
                    RelativeEntry entry = iterator.next();

                    ItemStack anchor = findAnchor(event, entry.anchor(), visibility);
                    if (anchor == null) {
                        continue;
                    }

                    ItemStack added = entry.added().create();

                    if (entry.placement() == RelativePlacement.AFTER) {
                        event.insertAfter(anchor, added, visibility);
                    } else {
                        event.insertBefore(anchor, added, visibility);
                    }

                    iterator.remove();
                    changed = true;
                }
            } while (changed);

            if (!pending.isEmpty()) {
                for (RelativeEntry entry : pending) {
                    ItemStack added = entry.added().create();

                    LogUtils.getLogger().warn(
                            "Failed to add item {} {} anchor item {} in NeoForge creative tab {} because the anchor was not present",
                            BuiltInRegistries.ITEM.getKey(added.getItem()),
                            entry.placement() == RelativePlacement.AFTER ? "after" : "before",
                            BuiltInRegistries.ITEM.getKey(entry.anchor().asItem()),
                            entry.tab().identifier()
                    );
                }
            }
        }

        private static @Nullable ItemStack findAnchor(BuildCreativeModeTabContentsEvent event, ItemLike item, CreativeModeTab.TabVisibility visibility) {
            if (visibility == CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS || visibility == CreativeModeTab.TabVisibility.PARENT_TAB_ONLY) {
                ItemStack parentAnchor = findAnchorIn(event.getParentEntries(), item);

                if (parentAnchor != null) {
                    return parentAnchor;
                }
            }

            if (visibility == CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS || visibility == CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY) {
                return findAnchorIn(event.getSearchEntries(), item);
            }

            return null;
        }

        private static @Nullable ItemStack findAnchorIn(Iterable<ItemStack> entries, ItemLike item) {
            Item target = item.asItem();

            for (ItemStack stack : entries) {
                if (stack.is(target)) {
                    return stack;
                }
            }

            return null;
        }

        private enum RelativePlacement {
            BEFORE,
            AFTER
        }

        private record RelativeEntry(RelativePlacement placement, ItemLike anchor, ItemStackTemplate added, ResourceKey<CreativeModeTab> tab) {}
    }

    public static class Packs implements HelpersImpl.Packs {

        public static List<Pair<Identifier, PackType>> PACK_LIST = new ArrayList<>();

        @Override
        public void add(Identifier id, PackType info) {
            PACK_LIST.add(Pair.of(id, info));
        }

        public static boolean getBoolean(PackType info) {
            return switch (info) {
                case REQUIRED_DATA, REQUIRED_RESOURCES -> true;
                case OPTIONAL_DATA, OPTIONAL_RESOURCES -> false;
            };
        }

        public static net.minecraft.server.packs.PackType getType(PackType info) {
            return switch (info) {
                case REQUIRED_DATA, OPTIONAL_DATA -> net.minecraft.server.packs.PackType.SERVER_DATA;
                case REQUIRED_RESOURCES, OPTIONAL_RESOURCES -> net.minecraft.server.packs.PackType.CLIENT_RESOURCES;
            };
        }

        @SubscribeEvent
        public static void addFeaturePacks(AddPackFindersEvent event) {
            for (Pair<Identifier, PackType> pair : PACK_LIST) {
                Identifier id = pair.getFirst();
                PackType info = pair.getSecond();

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

    public static class Networking implements HelpersImpl.Networking {

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

        @Override
        public boolean canSend(CustomPacketPayload payload, ServerPlayer player) {
            return player.connection.hasChannel(payload.type());
        }

        @Override
        public void send(CustomPacketPayload payload, ServerPlayer player) {
            player.connection.send(new ClientboundCustomPayloadPacket(payload));
        }

        @SubscribeEvent
        public static void register(RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar("1").optional();
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

    public static class ReloadListeners implements HelpersImpl.ReloadListeners {

        private static List<Pair<Identifier, PreparableReloadListener>> LISTENERS = new ArrayList<>();
        private static List<Pair<Identifier, Identifier>> ORDERING = new ArrayList<>();

        @Override
        public void addListener(Identifier id, PreparableReloadListener listener) {
            LISTENERS.add(Pair.of(id, listener));
        }

        @Override
        public void addOrdering(Identifier first, Identifier second) {
            ORDERING.add(Pair.of(first, second));
        }

        @SubscribeEvent
        public static void addServerReloadListeners(final AddServerReloadListenersEvent event) {
            LogUtils.getLogger().info("ran!");
            LISTENERS.forEach(pair -> event.addListener(pair.getFirst(), pair.getSecond()));
            ORDERING.forEach(pair -> event.addDependency(pair.getFirst(), pair.getSecond()));
        }
    }

    public static class DataRegistries implements HelpersImpl.DataRegistries {

        private static final List<Consumer<DataPackRegistryEvent.NewRegistry>> REGISTRATIONS = new ArrayList<>();
        @Override
        public <T> void register(ResourceKey<Registry<T>> key, Codec<T> codec) {
            REGISTRATIONS.add(event -> event.dataPackRegistry(key, codec));
        }

        @Override
        public <T> void registerSynced(ResourceKey<Registry<T>> key, Codec<T> serverCodec, Codec<T> clientCodec) {
            REGISTRATIONS.add(event -> event.dataPackRegistry(key, serverCodec, clientCodec));
        }

        @SubscribeEvent
        public static void registerDataRegistries(final DataPackRegistryEvent.NewRegistry event) {
            LogUtils.getLogger().info("ran!");
            REGISTRATIONS.forEach(consumer -> consumer.accept(event));
        }
    }

    public static class EntityData implements HelpersImpl.EntityData {

        @Override
        public void registerSerializer(Identifier id, Supplier<EntityDataSerializer<?>> serializer) {
            UnifiedRegistries.DeferredRegistry.create(id.getNamespace(), NeoForgeRegistries.ENTITY_DATA_SERIALIZERS).register(id.getPath(), serializer);
        }

    }
}
