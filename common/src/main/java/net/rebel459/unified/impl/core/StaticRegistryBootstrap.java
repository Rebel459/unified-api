package net.rebel459.unified.impl.core;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.rebel459.unified.api.core.SuppliedBlock;
import net.rebel459.unified.api.core.SuppliedItem;
import net.rebel459.unified.api.core.UnifiedRegistries;
import net.rebel459.unified.impl.platform.PlatformHandler;
import org.jetbrains.annotations.ApiStatus;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@ApiStatus.Internal
public final class StaticRegistryBootstrap {
    private static final Map<ResourceKey<? extends Registry<?>>, RegistryStage<?>> STAGES = new LinkedHashMap<>();
    private static final RegistryStage<BlockPlan> BLOCKS = stage(Registries.BLOCK, List.of(Registries.SOUND_EVENT), BlockPlan::register);
    private static final RegistryStage<ItemPlan> ITEMS = stage(Registries.ITEM, List.of(Registries.BLOCK), ItemPlan::register);
    private static boolean staging = true;

    private StaticRegistryBootstrap() {}

    public static UnifiedRegistries.Blocks blocks(String modId) { return new StagedBlocks(modId); }
    public static UnifiedRegistries.Items items(String modId) { return new StagedItems(modId); }

    public static synchronized void finish() {
        if (!staging) return;
        finishStages(new LinkedHashSet<>(), new LinkedHashSet<>());
        staging = false;
    }

    private static void finishStages(Set<ResourceKey<? extends Registry<?>>> completed,
            Set<ResourceKey<? extends Registry<?>>> visiting) {
        for (RegistryStage<?> registry : STAGES.values()) registry.finish(completed, visiting);
    }

    private static <P> RegistryStage<P> stage(ResourceKey<? extends Registry<?>> registry,
            List<ResourceKey<? extends Registry<?>>> dependencies, PlanRegistrar<P> registrar) {
        RegistryStage<P> stage = new RegistryStage<>(registry, dependencies, registrar);
        if (STAGES.putIfAbsent(registry, stage) != null) {
            throw new IllegalStateException("Static registry stage already exists for " + registry.identifier());
        }
        return stage;
    }

    private static final class StagedBlocks implements UnifiedRegistries.Blocks {
        private final String modId;
        private StagedBlocks(String modId) { this.modId = modId; }
        @Override public String modId() { return modId; }

        @Override
        public <T extends Block> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> function,
                                                        Supplier<BlockBehaviour.Properties> properties) {
            SuppliedBlock block = stage(path, path, function, properties, Optional.empty());
            items(modId).registerBlockItem(block, BlockItem::new, Item.Properties::new);
            return block;
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock register(String path,
                Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> properties,
                Supplier<BlockEntityType<Y>> type) {
            SuppliedBlock block = stage(path, path, function, properties, Optional.of(type));
            items(modId).registerBlockItem(block, BlockItem::new, Item.Properties::new);
            return block;
        }

        @Override
        public <T extends Block> SuppliedBlock registerWithoutItem(String path,
                Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> properties) {
            return registerWithoutItem(path, path, function, properties);
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock registerWithoutItem(String path,
                Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> properties,
                Supplier<BlockEntityType<Y>> type) {
            return registerWithoutItem(path, path, function, properties, type);
        }

        @Override
        public <T extends Block> SuppliedBlock registerWithoutItem(String blockPath, String itemPath,
                Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> properties) {
            return stage(blockPath, itemPath, function, properties, Optional.empty());
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock registerWithoutItem(String blockPath,
                String itemPath, Function<BlockBehaviour.Properties, T> function,
                Supplier<BlockBehaviour.Properties> properties, Supplier<BlockEntityType<Y>> type) {
            return stage(blockPath, itemPath, function, properties, Optional.of(type));
        }

        private SuppliedBlock stage(String blockPath, String itemPath,
                Function<BlockBehaviour.Properties, ? extends Block> factory,
                Supplier<BlockBehaviour.Properties> properties,
                Optional<? extends Supplier<? extends BlockEntityType<?>>> blockEntity) {
            synchronized (StaticRegistryBootstrap.class) {
                Identifier id = Identifier.fromNamespaceAndPath(modId, blockPath);
                SuppliedBlock claimed = DataRegistryClaims.block(id);
                if (claimed != null) return claimed;
                BlockItemId blockItemId = BlockItemId.create(
                        id, Identifier.fromNamespaceAndPath(modId, itemPath));
                if (!staging) {
                    return registerNow(PlatformHandler.INSTANCE.createBlocks(modId), blockItemId,
                            factory, properties, blockEntity);
                }
                BlockPlan plan = BLOCKS.get(id);
                if (plan == null) {
                    AtomicReference<Supplier<? extends Block>> block = new AtomicReference<>();
                    SuppliedBlock supplied = new SuppliedBlock(() -> BuiltInRegistries.BLOCK,
                            blockItemId, () -> resolve(block, id, "block"));
                    plan = new BlockPlan(supplied, block, factory, properties, blockEntity);
                    BLOCKS.put(id, plan);
                } else {
                    if (!DataRegistryClaims.isRegisteringBlock(id)) throw new IllegalStateException("Duplicate code block registration: " + id);
                    plan.factory = factory;
                    plan.properties = properties;
                    plan.blockEntity = blockEntity;
                }
                return plan.supplied;
            }
        }

        @Override
        public void addAlias(Identifier from, Identifier to) {
            PlatformHandler.INSTANCE.createBlocks(modId).addAlias(from, to);
        }
    }

    private static final class StagedItems implements UnifiedRegistries.Items {
        private final String modId;
        private StagedItems(String modId) { this.modId = modId; }
        @Override public String modId() { return modId; }

        @Override
        public SuppliedItem register(String path, Function<Item.Properties, Item> factory,
                                     Supplier<Item.Properties> properties) {
            Identifier id = Identifier.fromNamespaceAndPath(modId, path);
            return stage(id, platform -> platform.register(path, factory, properties));
        }

        @Override
        public SuppliedItem registerBlockItem(SuppliedBlock block,
                BiFunction<Block, Item.Properties, Item> function, Supplier<Item.Properties> properties) {
            return registerBlockItem(block.blockItemId(), block, function, properties);
        }

        @Override
        public <T extends Block> SuppliedItem registerBlockItem(BlockItemId id, Supplier<T> block,
                BiFunction<Block, Item.Properties, Item> function, Supplier<Item.Properties> properties) {
            return stage(id.item().identifier(),
                    platform -> platform.registerBlockItem(id, block, function, properties));
        }

        private SuppliedItem stage(Identifier id,
                Function<UnifiedRegistries.Items, SuppliedItem> registration) {
            synchronized (StaticRegistryBootstrap.class) {
                SuppliedItem claimed = DataRegistryClaims.item(id);
                if (claimed != null) return claimed;
                if (!staging) return registration.apply(PlatformHandler.INSTANCE.createItems(modId));
                ItemPlan plan = ITEMS.get(id);
                if (plan == null) {
                    AtomicReference<Supplier<? extends Item>> item = new AtomicReference<>();
                    SuppliedItem supplied = new SuppliedItem(() -> BuiltInRegistries.ITEM,
                            ResourceKey.create(Registries.ITEM, id), () -> resolve(item, id, "item"));
                    plan = new ItemPlan(supplied, item, registration);
                    ITEMS.put(id, plan);
                } else {
                    if (!DataRegistryClaims.isRegisteringItem(id)) throw new IllegalStateException("Duplicate code item registration: " + id);
                    plan.registration = registration;
                }
                return plan.supplied;
            }
        }

        @Override
        public void addAlias(Identifier from, Identifier to) {
            PlatformHandler.INSTANCE.createItems(modId).addAlias(from, to);
        }
    }

    private static final class BlockPlan {
        private final SuppliedBlock supplied;
        private final AtomicReference<Supplier<? extends Block>> block;
        private Function<BlockBehaviour.Properties, ? extends Block> factory;
        private Supplier<BlockBehaviour.Properties> properties;
        private Optional<? extends Supplier<? extends BlockEntityType<?>>> blockEntity;

        private BlockPlan(SuppliedBlock supplied, AtomicReference<Supplier<? extends Block>> block,
                Function<BlockBehaviour.Properties, ? extends Block> factory,
                Supplier<BlockBehaviour.Properties> properties,
                Optional<? extends Supplier<? extends BlockEntityType<?>>> blockEntity) {
            this.supplied = supplied;
            this.block = block;
            this.factory = factory;
            this.properties = properties;
            this.blockEntity = blockEntity;
        }

        private void register(Identifier id) {
            UnifiedRegistries.Blocks platform = PlatformHandler.INSTANCE.createBlocks(id.getNamespace());
            SuppliedBlock registered = DataRegistryClaims.platformRegistration(Registries.BLOCK, id,
                    () -> registerNow(platform, supplied.blockItemId(), factory, properties, blockEntity));
            block.set(registered::get);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static SuppliedBlock registerNow(UnifiedRegistries.Blocks platform, BlockItemId id,
            Function<BlockBehaviour.Properties, ? extends Block> factory, Supplier<BlockBehaviour.Properties> properties,
            Optional<? extends Supplier<? extends BlockEntityType<?>>> blockEntity) {
        String blockPath = id.block().identifier().getPath();
        String itemPath = id.item().identifier().getPath();
        if (blockEntity.isPresent()) {
            Supplier type = blockEntity.orElseThrow();
            return platform.registerWithoutItem(blockPath, itemPath, factory, properties, type);
        }
        return platform.registerWithoutItem(blockPath, itemPath, factory, properties);
    }

    private static final class ItemPlan {
        private final SuppliedItem supplied;
        private final AtomicReference<Supplier<? extends Item>> item;
        private Function<UnifiedRegistries.Items, SuppliedItem> registration;

        private ItemPlan(SuppliedItem supplied, AtomicReference<Supplier<? extends Item>> item,
                Function<UnifiedRegistries.Items, SuppliedItem> registration) {
            this.supplied = supplied;
            this.item = item;
            this.registration = registration;
        }

        private void register(Identifier id) {
            UnifiedRegistries.Items platform = PlatformHandler.INSTANCE.createItems(id.getNamespace());
            SuppliedItem registered = DataRegistryClaims.platformRegistration(Registries.ITEM, id,
                    () -> registration.apply(platform));
            item.set(registered::get);
        }
    }

    @FunctionalInterface
    private interface PlanRegistrar<P> {
        void register(P plan, Identifier id);
    }

    private static final class RegistryStage<P> {
        private final ResourceKey<? extends Registry<?>> registry;
        private final List<ResourceKey<? extends Registry<?>>> dependencies;
        private final PlanRegistrar<P> registrar;
        private final Map<Identifier, P> plans = new LinkedHashMap<>();

        private RegistryStage(ResourceKey<? extends Registry<?>> registry,
                List<ResourceKey<? extends Registry<?>>> dependencies, PlanRegistrar<P> registrar) {
            this.registry = registry;
            this.dependencies = List.copyOf(dependencies);
            this.registrar = registrar;
        }

        private P get(Identifier id) {
            return plans.get(id);
        }

        private void put(Identifier id, P plan) {
            plans.put(id, plan);
        }

        private void finish(Set<ResourceKey<? extends Registry<?>>> completed,
                Set<ResourceKey<? extends Registry<?>>> visiting) {
            if (completed.contains(registry)) return;
            if (!visiting.add(registry)) {
                throw new IllegalStateException("Circular static registry stage dependency involving " + registry.identifier());
            }
            for (ResourceKey<? extends Registry<?>> dependency : dependencies) {
                RegistryStage<?> stage = STAGES.get(dependency);
                if (stage != null) stage.finish(completed, visiting);
            }
            plans.forEach((id, plan) -> registrar.register(plan, id));
            plans.clear();
            visiting.remove(registry);
            completed.add(registry);
        }
    }

    private static <T> T resolve(AtomicReference<Supplier<? extends T>> reference, Identifier id, String type) {
        Supplier<? extends T> supplier = reference.get();
        if (supplier == null) throw new IllegalStateException("Requested staged " + type + " before registry bootstrap: " + id);
        return supplier.get();
    }
}
