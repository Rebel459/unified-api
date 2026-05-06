package net.rebel459.unified.platform;

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
import net.neoforged.neoforge.registries.DeferredRegister;
import net.rebel459.unified.util.BlockLike;
import net.rebel459.unified.util.SuppliedBlock;
import net.rebel459.unified.util.SuppliedItem;
import net.rebel459.unified.util.registry.SuppliedBlockImpl;
import net.rebel459.unified.util.registry.SuppliedItemImpl;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
        public <T extends Y> Supplier<T> register(String path, Supplier<T> value) {
            return DEFERRED.get(Pair.of(modId, registry)).register(path, value);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends Y> Holder<T> registerForHolder(String path, Supplier<T> value) {
            return DEFERRED.get(Pair.of(modId, registry)).register(path, value);
        }

        @Override
        public <T extends Y> Holder<T> registerHolder(String path, Supplier<T> value) {
            return registerForHolder(path, value);
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            DEFERRED.get(Pair.of(modId, registry)).addAlias(convertedFrom, convertedTo);
        }
    }

    public record Items(String modId) implements UnifiedRegistries.Items {

        @Override
        public SuppliedItem register(String path, Function<Item.Properties, Item> function, Supplier<Item.Properties> properties) {
            return new SuppliedItemImpl(ITEMS.get(modId).registerItem(path, function, properties));
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            ITEMS.get(modId).addAlias(convertedFrom, convertedTo);
        }

        @Override
        public <T extends BlockLike> SuppliedItem registerBlockItem(String path, Supplier<T> blockSupplier, Supplier<Item.Properties> properties) {
            return new SuppliedItemImpl(ITEMS.get(modId).registerItem(path, props -> new BlockItem(blockSupplier.get().asBlock(), props), () -> properties.get().useBlockDescriptionPrefix()));
        }
    }

    public record Blocks(String modId) implements UnifiedRegistries.Blocks {

        public static List<Pair<BlockEntityType<?>, Supplier<? extends Block>>> BLOCK_ENTITIES = new ArrayList<>();

        @Override
        public <T extends Block> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> blockProperties) {
            var block = BLOCKS.get(modId).registerBlock(path, function, blockProperties);
            var item = new SuppliedItemImpl(ITEMS.get(modId).registerSimpleBlockItem(path, block));
            return new SuppliedBlockImpl(block, item);
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> blockProperties, BlockEntityType<Y> type) {
            var block = register(path, function, blockProperties);
            BLOCK_ENTITIES.add(Pair.of(type, block));
            return block;
        }

        @Override
        public <T extends Block> SuppliedBlock registerWithoutItem(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> properties) {
            return new SuppliedBlockImpl(BLOCKS.get(modId).registerBlock(path, function, properties));
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock registerWithoutItem(String path, Function<BlockBehaviour.Properties, T> function, Supplier<BlockBehaviour.Properties> properties, BlockEntityType<Y> type) {
            var block = registerWithoutItem(path, function, properties);
            BLOCK_ENTITIES.add(Pair.of(type, block));
            return block;
        }

        @Override
        public <T extends Block> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> blockFunction, Supplier<BlockBehaviour.Properties> blockProperties, Supplier<Item.Properties> itemProperties) {
            return register(path, blockFunction, blockProperties, Item::new, itemProperties);
        }

        @Override
        public <T extends Block> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> blockFunction, Supplier<BlockBehaviour.Properties> blockProperties, Function<Item.Properties, Item> itemFunction) {
            return register(path, blockFunction, blockProperties, itemFunction, Item.Properties::new);
        }

        @Override
        public <T extends Block> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> blockFunction, Supplier<BlockBehaviour.Properties> blockProperties, Function<Item.Properties, Item> itemFunction, Supplier<Item.Properties> itemProperties) {
            var item = new Items(modId).register(path, itemFunction, itemProperties);
            return new SuppliedBlockImpl(BLOCKS.get(modId).registerBlock(path, blockFunction, blockProperties), item);
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> blockFunction, Supplier<BlockBehaviour.Properties> blockProperties, Supplier<Item.Properties> itemProperties, BlockEntityType<Y> type) {
            return register(path, blockFunction, blockProperties, Item::new, itemProperties, type);
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> blockFunction, Supplier<BlockBehaviour.Properties> blockProperties, Function<Item.Properties, Item> itemFunction, BlockEntityType<Y> type) {
            return register(path, blockFunction, blockProperties, itemFunction, Item.Properties::new, type);
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> blockFunction, Supplier<BlockBehaviour.Properties> blockProperties, Function<Item.Properties, Item> itemFunction, Supplier<Item.Properties> itemProperties, BlockEntityType<Y> type) {
            var block = register(path, blockFunction, blockProperties, itemFunction, itemProperties);
            BLOCK_ENTITIES.add(Pair.of(type, block));
            return block;
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            BLOCKS.get(modId).addAlias(convertedFrom, convertedTo);
        }

        @SubscribeEvent
        public static void modifyBlockEntities(BlockEntityTypeAddBlocksEvent event) {
            for (Pair<BlockEntityType<?>, Supplier<? extends Block>> pair : BLOCK_ENTITIES) {
                BlockEntityType<?> type = pair.getFirst();
                Block block = pair.getSecond().get();
                event.modify(type, block);
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
        public <T> Supplier<DataComponentType<T>> register(String path, UnaryOperator<DataComponentType.Builder<T>> unaryOperator) {
            return DATA_COMPONENTS.get(modId).registerComponentType(path, unaryOperator);
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            DATA_COMPONENTS.get(modId).addAlias(convertedFrom, convertedTo);
        }
    }

    public record EntityTypes(String modId) implements UnifiedRegistries.EntityTypes {

        @Override
        public @NotNull <T extends Entity> Supplier<EntityType<T>> register(String path, @NotNull EntityType.Builder<T> builder) {
            return DEFERRED.get(Pair.of(modId, BuiltInRegistries.ENTITY_TYPE)).register(path, () -> builder.build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(modId, path))));
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            DEFERRED.get(Pair.of(modId, BuiltInRegistries.ENTITY_TYPE)).addAlias(convertedFrom, convertedTo);
        }
    }

    public record BlockEntityTypes(String modId) implements UnifiedRegistries.BlockEntityTypes {

        @Override
        public @NotNull <T extends BlockEntity> Supplier<BlockEntityType<T>> register(String path, BlockEntityType.@NotNull BlockEntitySupplier<T> builder) {
            return register(path, builder, Set.of());
        }

        @Override
        public @NotNull <T extends BlockEntity> Supplier<BlockEntityType<T>> register(String path, BlockEntityType.@NotNull BlockEntitySupplier<T> builder, BlockLike... blocks) {
            Set<Block> set = new HashSet<>();
            for (BlockLike blockLike : blocks) {
                set.add(blockLike.asBlock());
            }
            return register(path, builder, set);
        }

        private @NotNull <T extends BlockEntity> Supplier<BlockEntityType<T>> register(String path, BlockEntityType.@NotNull BlockEntitySupplier<T> builder, Set<Block> set) {
            return DEFERRED.get(Pair.of(modId, BuiltInRegistries.BLOCK_ENTITY_TYPE)).register(path, () -> new BlockEntityType<>(builder, set));
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            DEFERRED.get(Pair.of(modId, BuiltInRegistries.BLOCK_ENTITY_TYPE)).addAlias(convertedFrom, convertedTo);
        }
    }

    public record SoundEvents(String modId) implements UnifiedRegistries.SoundEvents {

        @Override
        public Supplier<SoundEvent> register(String path) {
            return ((DeferredRegister<SoundEvent>) DEFERRED.get(Pair.of(modId, BuiltInRegistries.SOUND_EVENT))).register(path, SoundEvent::createVariableRangeEvent);
        }
        @Override
        public Supplier<SoundEvent> register(String path, float fixedRange) {
            return ((DeferredRegister<SoundEvent>) DEFERRED.get(Pair.of(modId, BuiltInRegistries.SOUND_EVENT))).register(path, () -> SoundEvent.createFixedRangeEvent(Identifier.fromNamespaceAndPath(modId, path), fixedRange));
        }

        @Override
        public Holder<SoundEvent> registerForHolder(String path) {
            return ((DeferredRegister<SoundEvent>) DEFERRED.get(Pair.of(modId, BuiltInRegistries.SOUND_EVENT))).register(path, () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(modId, path)));
        }
        @Override
        public Holder<SoundEvent> registerForHolder(String path, float fixedRange) {
            return ((DeferredRegister<SoundEvent>) DEFERRED.get(Pair.of(modId, BuiltInRegistries.SOUND_EVENT))).register(path, () -> SoundEvent.createFixedRangeEvent(Identifier.fromNamespaceAndPath(modId, path), fixedRange));
        }

        @Override
        public Holder<SoundEvent> registerHolder(String path) {
            return registerForHolder(path);
        }
        @Override
        public Holder<SoundEvent> registerHolder(String path, float fixedRange) {
            return registerForHolder(path);
        }
    }
}
