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
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.rebel459.unified.util.PackInfo;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class UnifiedRegistries {

    public interface ItemRegistry {
        String modId();

        Supplier<Item> register(String name, Function<Item.Properties, Item> function, Supplier<Item.Properties> properties);

        Supplier<BlockItem> registerBlockItem(String name, Supplier<Block> blockSupplier, Supplier<Item.Properties> properties);

        static ItemRegistry create(String modId) {
            return UnifiedFactory.getRegistries().createItemRegistry(modId);
        }
    }

    public interface BlockRegistry {
        String modId();

        <T extends Block> Supplier<T> register(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties blockProperties);

        <T extends Block> Supplier<T> registerWithoutItem(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties properties);

        static BlockRegistry create(String modId) {
            return UnifiedFactory.getRegistries().createBlockRegistry(modId);
        }
    }

    public interface CreativeRegistry {
        String modId();

        ResourceKey<CreativeModeTab> registerTab(String path, Supplier<? extends ItemLike> icon);

        static CreativeRegistry create(String modId) {
            return UnifiedFactory.getRegistries().createCreativeRegistry(modId);
        }
    }

    public interface ComponentRegistry {
        String modId();

        <T> Supplier<DataComponentType<T>> register(String string, UnaryOperator<DataComponentType.Builder<T>> unaryOperator);

        static ComponentRegistry create(String modId) {
            return UnifiedFactory.getRegistries().createComponentRegistry(modId);
        }
    }
}