package net.rebel459.unified.platform;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.rebel459.unified.util.impl.CompostingRegistryImpl;
import net.rebel459.unified.util.Item2ObjectMap;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class UnifiedRegistries {

    public interface ItemRegistry {
        String modId();

        Supplier<Item> register(String name, Function<Item.Properties, Item> function, Supplier<Item.Properties> properties);

        Supplier<BlockItem> registerBlockItem(String name, Supplier<Block> blockSupplier, Supplier<Item.Properties> properties);

        static ItemRegistry create(String modId) {
            return RegistryFactory.get().createItemRegistry(modId);
        }
    }

    public interface BlockRegistry {
        String modId();

        <T extends Block> Supplier<T> register(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties blockProperties);

        <T extends Block> Supplier<T> registerWithoutItem(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties properties);

        static BlockRegistry create(String modId) {
            return RegistryFactory.get().createBlockRegistry(modId);
        }
    }

    public interface FuelRegistry {

        void add(ItemLike item, int ticks);
        void add(TagKey<Item> item, int ticks);

        static FuelRegistry create() {
            return RegistryFactory.get().createFuelRegistry();
        }
    }

    public interface CreativeRegistry {

        void add(ResourceKey<CreativeModeTab> tab, ItemLike... items);
        void add(ResourceKey<CreativeModeTab> tab, ItemStack... items);
        void addAfter(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemLike... addedItems);
        void addAfter(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemStack... addedItems);
        void addBefore(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemLike... addedItems);
        void addBefore(ResourceKey<CreativeModeTab> tab, ItemLike existingItem, ItemStack... addedItems);

        ResourceKey<CreativeModeTab> registerTab(Identifier id, Supplier<? extends ItemLike> icon);

        static CreativeRegistry create() {
            return RegistryFactory.get().createCreativeRegistry();
        }
    }

    public interface CompostingRegistry extends Item2ObjectMap<Float> {
        static CompostingRegistry create() {
            return new CompostingRegistryImpl();
        }
    }

    public interface ComponentRegistry {
        String modId();

        <T> Supplier<DataComponentType<T>> register(String string, UnaryOperator<DataComponentType.Builder<T>> unaryOperator);

        static ComponentRegistry create(String modId) {
            return RegistryFactory.get().createComponentRegistry(modId);
        }
    }
}