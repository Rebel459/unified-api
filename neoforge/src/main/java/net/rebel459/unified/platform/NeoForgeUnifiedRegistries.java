package net.rebel459.unified.platform;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.rebel459.unified.util.SuppliedBlock;
import net.rebel459.unified.util.SuppliedBlockImpl;
import net.rebel459.unified.util.SuppliedItem;
import net.rebel459.unified.util.SuppliedItemImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class NeoForgeUnifiedRegistries {

    public static final Map<String, DeferredRegister.Items> ITEMS = new ConcurrentHashMap<>();
    public static final Map<String, DeferredRegister.Blocks> BLOCKS = new ConcurrentHashMap<>();
    public static final Map<String, DeferredRegister<CreativeModeTab>> CREATIVE_TABS = new ConcurrentHashMap<>();
    public static final Map<String, DeferredRegister.DataComponents> DATA_COMPONENTS = new ConcurrentHashMap<>();
    public static final Map<String, DeferredRegister<ParticleType<?>>> PARTICLES = new ConcurrentHashMap<>();
    public static final Map<String, DeferredRegister<MobEffect>> EFFECTS = new ConcurrentHashMap<>();
    public static final Map<String, DeferredRegister<EntityType<?>>> ENTITIES = new ConcurrentHashMap<>();
    public static final Map<String, DeferredRegister<BlockEntityType<?>>> BLOCK_ENTITIES = new ConcurrentHashMap<>();
    public static final Map<String, DeferredRegister<SoundEvent>> SOUND_EVENTS = new ConcurrentHashMap<>();
    public static final Map<String, DeferredRegister<MapCodec<? extends EnchantmentProvider>>> ENCHANTMENT_PROVIDERS = new ConcurrentHashMap<>();
    public static final Map<String, DeferredRegister<MapCodec<? extends LevelBasedValue>>> ENCHANTMENT_LEVEL_BASED_VALUES = new ConcurrentHashMap<>();
    public static final Map<String, DeferredRegister<MapCodec<? extends EnchantmentEntityEffect>>> ENCHANTMENT_ENTITY_EFFECTS = new ConcurrentHashMap<>();
    public static final Map<String, DeferredRegister<MapCodec<? extends EnchantmentValueEffect>>> ENCHANTMENT_VALUE_EFFECTS = new ConcurrentHashMap<>();
    public static final Map<String, DeferredRegister<MapCodec<? extends EnchantmentLocationBasedEffect>>> ENCHANTMENT_LOCATION_BASED_EFFECTS = new ConcurrentHashMap<>();

    public static void registerBus(String modId, IEventBus modEventBus) {
        DeferredRegister.Items items = ITEMS.computeIfAbsent(modId, string -> DeferredRegister.createItems(modId));
        DeferredRegister.Blocks blocks = BLOCKS.computeIfAbsent(modId, string -> DeferredRegister.createBlocks(modId));
        DeferredRegister<CreativeModeTab> creativeTabs = CREATIVE_TABS.computeIfAbsent(modId, string -> DeferredRegister.create(Registries.CREATIVE_MODE_TAB, modId));
        DeferredRegister.DataComponents dataComponents = DATA_COMPONENTS.computeIfAbsent(modId, string -> DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, modId));
        DeferredRegister<ParticleType<?>> particles = PARTICLES.computeIfAbsent(modId, string -> DeferredRegister.create(Registries.PARTICLE_TYPE, modId));
        DeferredRegister<MobEffect> effects = EFFECTS.computeIfAbsent(modId, string -> DeferredRegister.create(Registries.MOB_EFFECT, modId));
        DeferredRegister<EntityType<?>> entities = ENTITIES.computeIfAbsent(modId, string -> DeferredRegister.create(Registries.ENTITY_TYPE, modId));
        DeferredRegister<BlockEntityType<?>> blockEntities = BLOCK_ENTITIES.computeIfAbsent(modId, string -> DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, modId));
        DeferredRegister<SoundEvent> soundEvents = SOUND_EVENTS.computeIfAbsent(modId, string -> DeferredRegister.create(Registries.SOUND_EVENT, modId));
        DeferredRegister<MapCodec<? extends EnchantmentProvider>> enchantmentProviders = ENCHANTMENT_PROVIDERS.computeIfAbsent(modId, string -> DeferredRegister.create(Registries.ENCHANTMENT_PROVIDER_TYPE, modId));
        DeferredRegister<MapCodec<? extends LevelBasedValue>> enchantmentLevelBasedValues = ENCHANTMENT_LEVEL_BASED_VALUES.computeIfAbsent(modId, string -> DeferredRegister.create(Registries.ENCHANTMENT_LEVEL_BASED_VALUE_TYPE, modId));
        DeferredRegister<MapCodec<? extends EnchantmentEntityEffect>> enchantmentEntityEffects = ENCHANTMENT_ENTITY_EFFECTS.computeIfAbsent(modId, string -> DeferredRegister.create(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, modId));
        DeferredRegister<MapCodec<? extends EnchantmentValueEffect>> enchantmentValueEffects = ENCHANTMENT_VALUE_EFFECTS.computeIfAbsent(modId, string -> DeferredRegister.create(Registries.ENCHANTMENT_VALUE_EFFECT_TYPE, modId));
        DeferredRegister<MapCodec<? extends EnchantmentLocationBasedEffect>> enchantmentLocationBasedEffects = ENCHANTMENT_LOCATION_BASED_EFFECTS.computeIfAbsent(modId, string -> DeferredRegister.create(Registries.ENCHANTMENT_LOCATION_BASED_EFFECT_TYPE, modId));

        items.register(modEventBus);
        blocks.register(modEventBus);
        creativeTabs.register(modEventBus);
        dataComponents.register(modEventBus);
        particles.register(modEventBus);
        effects.register(modEventBus);
        entities.register(modEventBus);
        blockEntities.register(modEventBus);
        soundEvents.register(modEventBus);
        enchantmentProviders.register(modEventBus);
        enchantmentLevelBasedValues.register(modEventBus);
        enchantmentEntityEffects.register(modEventBus);
        enchantmentValueEffects.register(modEventBus);
        enchantmentLocationBasedEffects.register(modEventBus);
    }

    public record Items(String modId) implements UnifiedRegistries.Items {

        @Override
        public SuppliedItem register(String path, Function<Item.Properties, Item> function, Supplier<Item.Properties> properties) {
            return new SuppliedItemImpl(ITEMS.get(modId).registerItem(path, function, properties).getDelegate());
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            ITEMS.get(modId).addAlias(convertedFrom, convertedTo);
        }

        @Override
        public <T extends Block> SuppliedItem registerBlockItem(String path, Supplier<T> blockSupplier, Supplier<Item.Properties> properties) {
            return new SuppliedItemImpl(ITEMS.get(modId).registerSimpleBlockItem(path, blockSupplier, properties));
        }
    }

    public record Blocks(String modId) implements UnifiedRegistries.Blocks {

        public static List<Pair<BlockEntityType<?>, Supplier<? extends Block>>> BLOCK_ENTITIES = new ArrayList<>();

        @Override
        public <T extends Block> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties blockProperties) {
            SuppliedBlock blockHolder = registerWithoutItem(path, function, blockProperties);
            ITEMS.get(modId).registerSimpleBlockItem(path, blockHolder);
            return blockHolder;
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties blockProperties, BlockEntityType<Y> type) {
            var block = register(path, function, blockProperties);
            BLOCK_ENTITIES.add(Pair.of(type, block));
            return block;
        }

        @Override
        public <T extends Block> SuppliedBlock registerWithoutItem(String path, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties properties) {
            return new SuppliedBlockImpl(BLOCKS.get(modId).registerBlock(path, function, () -> properties));
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock registerWithoutItem(String path, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties properties, BlockEntityType<Y> type) {
            var block = registerWithoutItem(path, function, properties);
            BLOCK_ENTITIES.add(Pair.of(type, block));
            return block;
        }

        @Override
        public <T extends Block> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> blockFunction, BlockBehaviour.Properties blockProperties, Supplier<Item.Properties> itemProperties) {
            return register(path, blockFunction, blockProperties, Item::new, itemProperties);
        }

        @Override
        public <T extends Block> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> blockFunction, BlockBehaviour.Properties blockProperties, Function<Item.Properties, Item> itemFunction) {
            return register(path, blockFunction, blockProperties, itemFunction, Item.Properties::new);
        }

        @Override
        public <T extends Block> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> blockFunction, BlockBehaviour.Properties blockProperties, Function<Item.Properties, Item> itemFunction, Supplier<Item.Properties> itemProperties) {
            new Items(modId).register(path, itemFunction, itemProperties);
            return registerWithoutItem(path, blockFunction, blockProperties);
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> blockFunction, BlockBehaviour.Properties blockProperties, Supplier<Item.Properties> itemProperties, BlockEntityType<Y> type) {
            return register(path, blockFunction, blockProperties, Item::new, itemProperties, type);
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> blockFunction, BlockBehaviour.Properties blockProperties, Function<Item.Properties, Item> itemFunction, BlockEntityType<Y> type) {
            return register(path, blockFunction, blockProperties, itemFunction, Item.Properties::new, type);
        }

        @Override
        public <T extends Block, Y extends BlockEntity> SuppliedBlock register(String path, Function<BlockBehaviour.Properties, T> blockFunction, BlockBehaviour.Properties blockProperties, Function<Item.Properties, Item> itemFunction, Supplier<Item.Properties> itemProperties, BlockEntityType<Y> type) {
            new Items(modId).register(path, itemFunction, itemProperties);
            return registerWithoutItem(path, blockFunction, blockProperties, type);
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
            CREATIVE_TABS.get(id.getNamespace()).register(id.getPath(), () -> CreativeModeTab.builder()
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

    public record ParticleTypes(String modId) implements UnifiedRegistries.ParticleTypes {

        @Override
        public <T extends ParticleType> Supplier<T> register(String path, ParticleType type) {
            return (Supplier<T>) PARTICLES.get(modId).register(path, () -> type);
        }
    }

    public record MobEffects(String modId) implements UnifiedRegistries.MobEffects {

        @Override
        public Holder<MobEffect> register(String path, MobEffect effect) {
            return EFFECTS.get(modId).register(path, () -> effect);
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            EFFECTS.get(modId).addAlias(convertedFrom, convertedTo);
        }
    }

    public record EntityTypes(String modId) implements UnifiedRegistries.EntityTypes {

        @Override
        public @NotNull <T extends Entity> Supplier<EntityType<T>> register(String path, @NotNull EntityType.Builder<T> builder) {
            return ENTITIES.get(modId).register(path, () -> builder.build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(modId, path))));
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            ENTITIES.get(modId).addAlias(convertedFrom, convertedTo);
        }
    }

    public record BlockEntityTypes(String modId) implements UnifiedRegistries.BlockEntityTypes {

        @Override
        public @NotNull <T extends BlockEntity> Supplier<BlockEntityType<T>> register(String path, BlockEntityType.@NotNull BlockEntitySupplier<T> builder) {
            return register(path, builder, Set.of());
        }

        @Override
        public @NotNull <T extends BlockEntity> Supplier<BlockEntityType<T>> register(String path, BlockEntityType.@NotNull BlockEntitySupplier<T> builder, Block... blocks) {
            return register(path, builder, Set.of(blocks));
        }

        private @NotNull <T extends BlockEntity> Supplier<BlockEntityType<T>> register(String path, BlockEntityType.@NotNull BlockEntitySupplier<T> builder, Set<Block> set) {
            return BLOCK_ENTITIES.get(modId).register(path, () -> new BlockEntityType<>(builder, set));
        }

        @Override
        public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
            BLOCK_ENTITIES.get(modId).addAlias(convertedFrom, convertedTo);
        }
    }

    public record SoundEvents(String modId) implements UnifiedRegistries.SoundEvents {

        @Override
        public Supplier<SoundEvent> register(String path) {
            return SOUND_EVENTS.get(modId).register(path, SoundEvent::createVariableRangeEvent);
        }

        @Override
        public Holder<SoundEvent> registerHolder(String path) {
            Identifier id = Identifier.fromNamespaceAndPath(modId, path);
            return SOUND_EVENTS.get(modId).register(path, () -> SoundEvent.createVariableRangeEvent(id)).getDelegate();
        }
    }

    public record EnchantmentCodecs(String modId) implements UnifiedRegistries.EnchantmentCodecs {

        @Override
        public void registerProvider(String path, MapCodec<? extends EnchantmentProvider> codec) {
            ENCHANTMENT_PROVIDERS.get(modId).register(path, () -> codec);
        }

        @Override
        public void registerLevelBasedValue(String path, MapCodec<? extends LevelBasedValue> codec) {
            ENCHANTMENT_LEVEL_BASED_VALUES.get(modId).register(path, () -> codec);
        }

        @Override
        public void registerEntityEffect(String path, MapCodec<? extends EnchantmentEntityEffect> codec) {
            ENCHANTMENT_ENTITY_EFFECTS.get(modId).register(path, () -> codec);
        }

        @Override
        public void registerValueEffect(String path, MapCodec<? extends EnchantmentValueEffect> codec) {
            ENCHANTMENT_VALUE_EFFECTS.get(modId).register(path, () -> codec);
        }

        @Override
        public void registerLocationBasedEffect(String path, MapCodec<? extends EnchantmentLocationBasedEffect> codec) {
            ENCHANTMENT_LOCATION_BASED_EFFECTS.get(modId).register(path, () -> codec);
        }
    }
}