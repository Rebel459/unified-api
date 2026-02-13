package net.rebel459.unified.platform;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public static void registerBus(String modId, IEventBus modEventBus) {
        DeferredRegister.Items items = ITEMS.computeIfAbsent(modId, string -> DeferredRegister.createItems(modId));
        DeferredRegister.Blocks blocks = BLOCKS.computeIfAbsent(modId, string -> DeferredRegister.createBlocks(modId));
        DeferredRegister<CreativeModeTab> creativeTabs = CREATIVE_TABS.computeIfAbsent(modId, string -> DeferredRegister.create(Registries.CREATIVE_MODE_TAB, modId));
        DeferredRegister.DataComponents components = DATA_COMPONENTS.computeIfAbsent(modId, string -> DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, modId));
        DeferredRegister<ParticleType<?>> particles = PARTICLES.computeIfAbsent(modId, string -> DeferredRegister.create(Registries.PARTICLE_TYPE, modId));
        DeferredRegister<MobEffect> effects = EFFECTS.computeIfAbsent(modId, string -> DeferredRegister.create(Registries.MOB_EFFECT, modId));
        DeferredRegister<EntityType<?>> entities = ENTITIES.computeIfAbsent(modId, string -> DeferredRegister.create(Registries.ENTITY_TYPE, modId));

        items.register(modEventBus);
        blocks.register(modEventBus);
        creativeTabs.register(modEventBus);
        components.register(modEventBus);
        particles.register(modEventBus);
        effects.register(modEventBus);
        entities.register(modEventBus);
    }

    public record Items(String modId) implements UnifiedRegistries.Items {

        @Override
        public Supplier<Item> register(String name, Function<Item.Properties, Item> function, Supplier<Item.Properties> properties) {
            return ITEMS.get(modId).registerItem(name, function, properties);
        }

        @Override
        public <T extends Block> Supplier<BlockItem> registerBlockItem(String name, Supplier<T> blockSupplier, Supplier<Item.Properties> properties) {
            return ITEMS.get(modId).registerSimpleBlockItem(name, blockSupplier, properties);
        }
    }

    public record Blocks(String modId) implements UnifiedRegistries.Blocks {

        public static List<Pair<BlockEntityType<?>, Supplier<? extends Block>>> BLOCK_ENTITIES = new ArrayList<>();

        @Override
        public <T extends Block> Supplier<T> register(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties blockProperties) {
            Supplier<T> blockHolder = registerWithoutItem(name, function, blockProperties);
            ITEMS.get(modId).registerSimpleBlockItem(name, blockHolder);
            return blockHolder;
        }

        @Override
        public <T extends Block, Y extends BlockEntity> Supplier<T> register(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties blockProperties, BlockEntityType<Y> type) {
            var block = register(name, function, blockProperties);
            BLOCK_ENTITIES.add(Pair.of(type, block));
            return block;
        }

        @Override
        public <T extends Block> Supplier<T> registerWithoutItem(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties properties) {
            return BLOCKS.get(modId).registerBlock(name, function, () -> properties);
        }

        @Override
        public <T extends Block, Y extends BlockEntity> Supplier<T> registerWithoutItem(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties properties, BlockEntityType<Y> type) {
            var block = registerWithoutItem(name, function, properties);
            BLOCK_ENTITIES.add(Pair.of(type, block));
            return block;
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
        public ResourceKey<CreativeModeTab> registerTab(String path, Supplier<? extends ItemLike> icon) {
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

    public record ItemComponents(String modId) implements UnifiedRegistries.ItemComponents {

        @Override
        public <T> Supplier<DataComponentType<T>> register(String string, UnaryOperator<DataComponentType.Builder<T>> unaryOperator) {
            return DATA_COMPONENTS.get(modId).registerComponentType(string, unaryOperator);
        }
    }

    public record Particles(String modId) implements UnifiedRegistries.Particles {

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
    }

    public record EntityTypes(String modId) implements UnifiedRegistries.EntityTypes {

        @Override
        public @NotNull <T extends Entity> Supplier<EntityType<T>> register(String path, @NotNull EntityType.Builder<T> builder) {
            return ENTITIES.get(modId).register(path, () -> builder.build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(modId, path))));
        }
    }
}