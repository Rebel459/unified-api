package net.rebel459.unified.platform;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.rebel459.unified.util.registry.DataRegistryClaims;
import net.rebel459.unified.util.registry.Supplied;
import net.rebel459.unified.util.registry.SuppliedBlock;
import net.rebel459.unified.util.registry.SuppliedItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

final class FabricRegistryBootstrap {
    private static final Object LOCK = new Object();

    private static final DeferredQueue<Block, SuppliedBlock> BLOCKS = new DeferredQueue<Block, SuppliedBlock>(
            BuiltInRegistries.BLOCK,
            (ResourceKey<Block> key, Supplier<Block> value) -> new SuppliedBlock(() -> BuiltInRegistries.BLOCK, key, value, null)
    );
    private static final DeferredQueue<Item, SuppliedItem> ITEMS = new DeferredQueue<Item, SuppliedItem>(
            BuiltInRegistries.ITEM,
            (ResourceKey<Item> key, Supplier<Item> value) -> new SuppliedItem(() -> BuiltInRegistries.ITEM, key, value)
    );

    private static boolean staging = true;

    private FabricRegistryBootstrap() {}

    static boolean isStaging() {
        return staging;
    }

    static void finish() {
        synchronized (LOCK) {
            if (!staging) return;

            BLOCKS.commitAll();
            ITEMS.commitAll();
            staging = false;
        }
    }

    static SuppliedItem stageItem(Identifier id, Function<Item.Properties, Item> function,
                                  Supplier<Item.Properties> properties) {
        synchronized (LOCK) {
            ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
            return ITEMS.stage(key,
                    () -> net.minecraft.world.item.Items.registerItem(key, function, properties.get().setId(key)),
                    DataRegistryClaims.isRegisteringItem(id), "item");
        }
    }

    static SuppliedItem stageBlockItem(Identifier id, Supplier<? extends Block> block,
                                       BiFunction<Block, Item.Properties, Item> function,
                                       Supplier<Item.Properties> properties) {
        synchronized (LOCK) {
            ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
            boolean replace = DataRegistryClaims.isRegisteringItem(id) || DataRegistryClaims.isRegisteringBlock(id);
            return ITEMS.stage(key,
                    () -> net.minecraft.world.item.Items.registerBlock(block.get(), function, properties.get()),
                    replace, "item");
        }
    }

    static SuppliedBlock stageBlock(Identifier id, Function<BlockBehaviour.Properties, ? extends Block> function,
                                    Supplier<BlockBehaviour.Properties> properties, boolean registerItem) {
        synchronized (LOCK) {
            ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
            SuppliedBlock block = BLOCKS.stage(key,
                    () -> Registry.register(BuiltInRegistries.BLOCK, key, function.apply(properties.get().setId(key))),
                    DataRegistryClaims.isRegisteringBlock(id), "block");
            configureBlockItem(id, block, registerItem);
            return block;
        }
    }

    static SuppliedBlock stageCustomBlock(Identifier id, Function<BlockBehaviour.Properties, ? extends Block> function,
                                          Supplier<BlockBehaviour.Properties> blockProperties,
                                          Function<Item.Properties, Item> itemFunction,
                                          Supplier<Item.Properties> itemProperties) {
        SuppliedBlock block = stageBlock(id, function, blockProperties, false);
        SuppliedItem item = stageBlockItem(id, block, (_, properties) -> itemFunction.apply(properties), itemProperties);
        DataRegistryClaims.setBlockItem(block, item);
        return block;
    }

    static void afterBlockRegistration(Identifier id, Consumer<Block> action) {
        synchronized (LOCK) {
            if (staging && BLOCKS.afterCommit(id, action)) return;
            action.accept(BuiltInRegistries.BLOCK.getValueOrThrow(ResourceKey.create(Registries.BLOCK, id)));
        }
    }

    private static void configureBlockItem(Identifier id, SuppliedBlock block, boolean registerItem) {
        SuppliedItem current = DataRegistryClaims.blockItem(block);
        if (!registerItem) {
            if (current != null) ITEMS.remove(id, current);
            DataRegistryClaims.setBlockItem(block, null);
            return;
        }

        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        SuppliedItem item = ITEMS.stage(key,
                () -> net.minecraft.world.item.Items.registerBlock(block.get(), new Item.Properties()),
                DataRegistryClaims.isRegisteringBlock(id), "item");
        DataRegistryClaims.setBlockItem(block, item);
    }

    @FunctionalInterface
    interface SuppliedFactory<T, S extends Supplied<T>> {
        S create(ResourceKey<T> key, Supplier<T> value);
    }

    /** Generic stable-reference queue reusable by future JSON-driven registry types. */
    static final class DeferredQueue<T, S extends Supplied<T>> {
        private final Registry<T> registry;
        private final SuppliedFactory<T, S> suppliedFactory;
        private final Map<Identifier, Entry<T, S>> entries = new LinkedHashMap<>();

        DeferredQueue(Registry<T> registry, SuppliedFactory<T, S> suppliedFactory) {
            this.registry = registry;
            this.suppliedFactory = suppliedFactory;
        }

        S stage(ResourceKey<T> key, Supplier<? extends T> registration, boolean replace, String typeName) {
            Identifier id = key.identifier();
            Entry<T, S> existing = entries.get(id);
            if (existing != null) {
                if (!replace) throw new IllegalStateException("Duplicate code " + typeName + " registration: " + id);
                existing.registration = registration;
                existing.afterCommit.clear();
                return existing.supplied;
            }

            AtomicReference<T> value = new AtomicReference<>();
            S supplied = suppliedFactory.create(key, () -> resolved(value, id));
            entries.put(id, new Entry<>(supplied, value, registration));
            return supplied;
        }

        boolean afterCommit(Identifier id, Consumer<T> action) {
            Entry<T, S> entry = entries.get(id);
            if (entry == null) return false;
            entry.afterCommit.add(action);
            return true;
        }

        void remove(Identifier id, S expected) {
            Entry<T, S> entry = entries.get(id);
            if (entry != null && entry.supplied == expected) entries.remove(id);
        }

        void commitAll() {
            for (Entry<T, S> entry : entries.values()) {
                T value = entry.registration.get();
                entry.value.set(value);
                entry.afterCommit.forEach(action -> action.accept(value));
            }
            entries.clear();
        }

    }

    private static final class Entry<T, S extends Supplied<T>> {
        private final S supplied;
        private final AtomicReference<T> value;
        private final List<Consumer<T>> afterCommit = new ArrayList<>();
        private Supplier<? extends T> registration;

        private Entry(S supplied, AtomicReference<T> value, Supplier<? extends T> registration) {
            this.supplied = supplied;
            this.value = value;
            this.registration = registration;
        }
    }

    private static <T> T resolved(AtomicReference<T> reference, Identifier id) {
        T value = reference.get();
        if (value == null) {
            throw new IllegalStateException("Registry value requested before static registration completed: " + id);
        }
        return value;
    }
}
