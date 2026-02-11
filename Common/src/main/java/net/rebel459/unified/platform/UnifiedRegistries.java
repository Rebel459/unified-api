package net.rebel459.unified.platform;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

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
        <T extends Block, Y extends BlockEntity> Supplier<T> register(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties blockProperties, BlockEntityType<Y> type);

        <T extends Block> Supplier<T> registerWithoutItem(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties properties);
        <T extends Block, Y extends BlockEntity> Supplier<T> registerWithoutItem(String name, Function<BlockBehaviour.Properties, T> function, BlockBehaviour.Properties properties, BlockEntityType<Y> type);

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

    public interface ParticleRegistry {
        String modId();

        <T extends ParticleType> Supplier<T> register(String path, ParticleType type);

        static ParticleRegistry create(String modId) {
            return UnifiedFactory.getRegistries().createParticleRegistry(modId);
        }
    }
}