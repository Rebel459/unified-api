package net.rebel459.unified.neoforge.core;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.rebel459.unified.api.core.UnifiedRegistries;
import net.rebel459.unified.api.util.BlockLike;
import net.rebel459.unified.api.core.Supplied;
import net.rebel459.unified.api.core.SuppliedBlock;
import net.rebel459.unified.api.core.SuppliedItem;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class NeoForgeUnifiedRegistries {

    public static final Map<Pair<String, Registry<?>>, DeferredRegister> DEFERRED = new ConcurrentHashMap<>();
    public static final Map<String, DeferredRegister.Items> ITEMS = new ConcurrentHashMap<>();
    public static final Map<String, DeferredRegister.Blocks> BLOCKS = new ConcurrentHashMap<>();
    public static final Map<String, DeferredRegister.DataComponents> DATA_COMPONENTS = new ConcurrentHashMap<>();

    public static void registerBus(String modId, IEventBus modEventBus) {
        registerBus(modId, modEventBus, List.of());
    }

    @SafeVarargs
    public static <T extends Registry<?>> void registerBus(String modId, IEventBus modEventBus, T... registries) {
        registerBus(modId, modEventBus, Arrays.stream(registries).toList());
    }

    private static <T extends Registry<?>> void registerBus(String modId, IEventBus modEventBus, List<T> registries) {
        DeferredRegister.Items items = ITEMS.computeIfAbsent(modId, string -> DeferredRegister.createItems(modId));
        DeferredRegister.Blocks blocks = BLOCKS.computeIfAbsent(modId, string -> DeferredRegister.createBlocks(modId));
        DeferredRegister.DataComponents dataComponents = DATA_COMPONENTS.computeIfAbsent(modId, string -> DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, modId));

        for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
            DeferredRegister<?> deferred = DEFERRED.computeIfAbsent(Pair.of(modId, registry), _ -> DeferredRegister.create(registry, modId));
            deferred.register(modEventBus);
        }
        for (Registry<?> registry : registries) {
            DeferredRegister<?> deferred = DEFERRED.computeIfAbsent(Pair.of(modId, registry), _ -> DeferredRegister.create(registry, modId));
            deferred.register(modEventBus);
        }

        items.register(modEventBus);
        blocks.register(modEventBus);
        dataComponents.register(modEventBus);
    }

    public record DeferredRegistry<Y>(String modId, Registry<?> registry) implements UnifiedRegistries.DeferredRegistry<Y> {

        @Override
        @SuppressWarnings("unchecked")
        public <T extends Y> Supplied<T> register(String path, Supplier<T> value) {
            var deferredRegistry = DEFERRED.get(Pair.of(modId, registry));
            var deferred = deferredRegistry.register(path, value);
            return new Supplied<T>(deferredRegistry.getRegistry(), deferred.getKey(), deferred);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends Y> Holder<T> registerForHolder(String path, Supplier<T> value) {
            return DEFERRED.get(Pair.of(modId, registry)).register(path, value);
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            DEFERRED.get(Pair.of(modId, registry)).addAlias(convertedFrom, convertedTo);
        }
    }

    public record Items(String modId) implements UnifiedRegistries.Items {

        @Override
        public SuppliedItem register(String path, Function<Item.Properties, Item> function, Supplier<Item.Properties> properties) {
            var registry = ITEMS.get(modId);
            var item = ITEMS.get(modId).registerItem(path, function, properties);
            return new SuppliedItem(registry.getRegistry(), item.getKey(), item);
        }

        @Override
        public SuppliedItem registerBlockItem(SuppliedBlock block, BiFunction<Block, Item.Properties, Item> function, Supplier<Item.Properties> properties) {
            return registerBlockItem(block.identifier().getPath(), block, function, properties);
        }

        @Override
        public <T extends Block> SuppliedItem registerBlockItem(String path, Supplier<T> block, BiFunction<Block, Item.Properties, Item> function, Supplier<Item.Properties> properties) {
            var registry = ITEMS.get(modId);
            var item = registry.registerItem(path, settings -> function.apply(block.get(), settings), () -> properties.get().useBlockDescriptionPrefix().requiredFeatures(block.get().requiredFeatures()));
            return new SuppliedItem(registry.getRegistry(), item.getKey(), item);
        }

        @Override
        @Deprecated
        public SuppliedItem registerBlockItem(SuppliedBlock block, Supplier<Item.Properties> properties) {
            return registerBlockItem(block, BlockItem::new, properties);
        }

        @Override
        @Deprecated
        public <T extends Block> SuppliedItem registerBlockItem(String path, Supplier<T> block, Supplier<Item.Properties> properties) {
            return registerBlockItem(path, block, BlockItem::new, properties);
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            ITEMS.get(modId).addAlias(convertedFrom, convertedTo);
        }
    }

    public record Blocks(String modId) implements UnifiedRegistries.Blocks {

        public static List<Pair<Supplier<? extends BlockEntityType<?>>, Supplier<? extends Block>>> BLOCK_ENTITIES = new ArrayList<>();

        @Override
        public <T extends Block> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> blockProperties) {
            var blockRegistry = BLOCKS.get(modId);
            var itemRegistry = ITEMS.get(modId);
            var block = blockRegistry.registerBlock(path, function, blockProperties);
            var item = itemRegistry.registerSimpleBlockItem(path, block);
            var suppliedItem = new SuppliedItem(itemRegistry.getRegistry(), item.getKey(), item);
            return new SuppliedBlock(blockRegistry.getRegistry(), block.getKey(), block, suppliedItem);
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> blockProperties, Supplier<BlockEntityType<Y>> type) {
            var block = register(path, function, blockProperties);
            BLOCK_ENTITIES.add(Pair.of(type, block));
            return block;
        }

        @Override
        public <T extends Block> SuppliedBlock registerWithoutItem(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> properties) {
            var registry = BLOCKS.get(modId);
            var block = registry.registerBlock(path, function, properties);
            return new SuppliedBlock(registry.getRegistry(), block.getKey(), block, null);
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock registerWithoutItem(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> properties, Supplier<BlockEntityType<Y>> type) {
            var block = registerWithoutItem(path, function, properties);
            BLOCK_ENTITIES.add(Pair.of(type, block));
            return block;
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            BLOCKS.get(modId).addAlias(convertedFrom, convertedTo);
        }

        @SubscribeEvent
        public static void modifyBlockEntities(BlockEntityTypeAddBlocksEvent event) {
            for (Pair<Supplier<? extends BlockEntityType<?>>, Supplier<? extends Block>> pair : BLOCK_ENTITIES) {
                Supplier<? extends BlockEntityType<?>> type = pair.getFirst();
                Block block = pair.getSecond().get();
                event.modify(type.get(), block);
            }
        }
    }

    public record CreativeTabs(String modId) implements UnifiedRegistries.CreativeTabs {

        @Override
        public ResourceKey<CreativeModeTab> register(String path, Supplier<? extends ItemLike> icon) {
            Identifier id = Identifier.fromNamespaceAndPath(modId, path);
            DEFERRED.get(Pair.of(modId, BuiltInRegistries.CREATIVE_MODE_TAB)).register(id.getPath(), () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + id.getNamespace() + "." + id.getPath()))
                    .icon(() -> new ItemStack(icon.get()))
                    .displayItems((params, output) -> {
                    })
                    .build());
            return ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), id);
        }
    }

    public record DataComponentTypes(String modId) implements UnifiedRegistries.DataComponentTypes {

        @Override
        public <T> Supplied<DataComponentType<T>> register(String path, UnaryOperator<DataComponentType.Builder<T>> unaryOperator) {
            var registry = DATA_COMPONENTS.get(modId);
            var component = registry.register(path, () -> unaryOperator.apply(DataComponentType.builder()).build());
            return new Supplied<>(registry.getRegistry(), component.getKey(), component);
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            DATA_COMPONENTS.get(modId).addAlias(convertedFrom, convertedTo);
        }
    }

    public record EntityTypes(String modId) implements UnifiedRegistries.EntityTypes {

        private static final List<Pair<Supplied<? extends EntityType<? extends LivingEntity>>, Supplier<AttributeSupplier>>> ENTITY_ATTRIBUTES = new ArrayList<>();

        @Override
        public @NotNull <T extends Entity> Supplied<EntityType<T>> register(String path, @NotNull EntityType.Builder<T> builder) {
            DeferredRegister<EntityType<T>> registry = DEFERRED.get(Pair.of(modId, BuiltInRegistries.ENTITY_TYPE));
            var entityType = registry.register(path, () -> builder.build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(modId, path))));
            return new Supplied<>(registry.getRegistry(), entityType.getKey(), entityType);
        }

        @Override
        public <T extends LivingEntity> Supplied<EntityType<T>> register(String path, EntityType.Builder<T> builder, Supplier<AttributeSupplier> attributes) {
            Supplied<EntityType<T>> entity = register(path, builder);
            ENTITY_ATTRIBUTES.add(Pair.of(entity, attributes));
            return entity;
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            DEFERRED.get(Pair.of(modId, BuiltInRegistries.ENTITY_TYPE)).addAlias(convertedFrom, convertedTo);
        }

        @SubscribeEvent
        public static void createEntityAttributes(EntityAttributeCreationEvent event) {
            for (Pair<Supplied<? extends EntityType<? extends LivingEntity>>, Supplier<AttributeSupplier>> pair : ENTITY_ATTRIBUTES) {
                event.put(pair.getFirst().get(), pair.getSecond().get());
            }
        }
    }

    public record BlockEntityTypes(String modId) implements UnifiedRegistries.BlockEntityTypes {

        @Override
        public @NotNull <T extends BlockEntity> Supplied<BlockEntityType<T>> register(String path, BlockEntityType.@NotNull BlockEntitySupplier<T> builder) {
            return register(path, builder, Set.of());
        }

        @Override
        public @NotNull <T extends BlockEntity> Supplied<BlockEntityType<T>> register(String path, BlockEntityType.@NotNull BlockEntitySupplier<T> builder, BlockLike... blocks) {
            Set<Block> set = new HashSet<>();
            for (BlockLike blockLike : blocks) {
                set.add(blockLike.asBlock());
            }
            return register(path, builder, set);
        }

        private @NotNull <T extends BlockEntity> Supplied<BlockEntityType<T>> register(String path, BlockEntityType.@NotNull BlockEntitySupplier<T> builder, Set<Block> set) {
            DeferredRegister<BlockEntityType<T>> registry = DEFERRED.get(Pair.of(modId, BuiltInRegistries.BLOCK_ENTITY_TYPE));
            var blockEntity = registry.register(path, () -> new BlockEntityType<>(builder, set));
            return new Supplied<>(registry.getRegistry(), blockEntity.getKey(), blockEntity);
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            DEFERRED.get(Pair.of(modId, BuiltInRegistries.BLOCK_ENTITY_TYPE)).addAlias(convertedFrom, convertedTo);
        }
    }

    public record SoundEvents(String modId) implements UnifiedRegistries.SoundEvents {

        @Override
        public Supplied<SoundEvent> register(String path) {
            DeferredRegister<SoundEvent> registry = DEFERRED.get(Pair.of(modId, BuiltInRegistries.SOUND_EVENT));
            var soundEvent = registry.register(path, SoundEvent::createVariableRangeEvent);
            return new Supplied<>(registry.getRegistry(), soundEvent.getKey(), soundEvent);
        }
        @Override
        public Supplied<SoundEvent> register(String path, float fixedRange) {
            DeferredRegister<SoundEvent> registry = DEFERRED.get(Pair.of(modId, BuiltInRegistries.SOUND_EVENT));
            var soundEvent = registry.register(path, () -> SoundEvent.createFixedRangeEvent(Identifier.fromNamespaceAndPath(modId, path), fixedRange));
            return new Supplied<>(registry.getRegistry(), soundEvent.getKey(), soundEvent);
        }

        @Override
        public Holder<SoundEvent> registerForHolder(String path) {
            return ((DeferredRegister<SoundEvent>) DEFERRED.get(Pair.of(modId, BuiltInRegistries.SOUND_EVENT))).register(path, () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(modId, path)));
        }
        @Override
        public Holder<SoundEvent> registerForHolder(String path, float fixedRange) {
            return ((DeferredRegister<SoundEvent>) DEFERRED.get(Pair.of(modId, BuiltInRegistries.SOUND_EVENT))).register(path, () -> SoundEvent.createFixedRangeEvent(Identifier.fromNamespaceAndPath(modId, path), fixedRange));
        }
    }
}
