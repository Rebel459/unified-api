package net.rebel459.unified.impl.registry;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.codec.UnifiedCodecs;
import net.rebel459.unified.impl.core.DataRegistryClaims;
import net.rebel459.unified.api.core.RegistryResourceListener;
import net.rebel459.unified.api.core.UnifiedRegistries;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class ItemRegistry extends RegistryResourceListener<ItemRegistry.Definition> {
    private static final MapCodec<Optional<Identifier>> BLOCK_ID_CODEC =
            Identifier.CODEC.optionalFieldOf("block");
    private static final MapCodec<Optional<Identifier>> TYPE_ID_CODEC =
            Identifier.CODEC.optionalFieldOf("type");
    private static final MapCodec<Map<DataComponentType<?>, Object>> PROPERTIES_CODEC =
            DataComponentType.VALUE_MAP_CODEC.optionalFieldOf("properties")
                    .xmap(properties -> properties.orElse(Map.of()),
                            properties -> properties.isEmpty() ? Optional.empty() : Optional.of(properties));
    private static final Codec<Definition> ITEM_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtensibleCodecs.ITEM_TYPES.mapCodec(Identifier.withDefaultNamespace("item")).forGetter(Definition::type),
            PROPERTIES_CODEC.forGetter(Definition::properties)
    ).apply(instance, Definition::item));
    private static final Codec<Definition> BLOCK_ITEM_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UnifiedCodecs.supplied(BuiltInRegistries.BLOCK).fieldOf("block")
                    .forGetter(definition -> definition.blockItem().orElseThrow().block()),
            ExtensibleCodecs.BLOCK_ITEM_TYPES.mapCodec(Identifier.withDefaultNamespace("block_item"))
                    .forGetter(definition -> definition.blockItem().orElseThrow().type()),
            PROPERTIES_CODEC.forGetter(Definition::properties)
    ).apply(instance, Definition::blockItem));
    public static final Codec<Definition> CODEC = Codec.either(BLOCK_ITEM_CODEC, ITEM_CODEC).xmap(
            value -> value.map(Function.identity(), Function.identity()),
            definition -> definition.isBlockItem() ? Either.left(definition) : Either.right(definition)
    );

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Unified.MOD_ID, "items");

    public ItemRegistry() {
        super(ID, CODEC, BlockRegistry.ID, EntityTypeRegistry.ID);
    }

    @Override
    protected void register(Identifier id, DeferredDeclaration<Definition> declaration) {
        Supplier<Item.Properties> properties = () -> createProperties(declaration.get().properties());
        DataRegistryClaims.registerItem(id, () -> {
            UnifiedRegistries.Items items = UnifiedRegistries.Items.create(id.getNamespace());
            Optional<Identifier> blockId = declaration.decode(BLOCK_ID_CODEC);
            Optional<Identifier> typeId = declaration.decode(TYPE_ID_CODEC);
            boolean blockItem = typeId.map(ExtensibleCodecs.BLOCK_ITEM_TYPES::contains)
                    .orElseGet(blockId::isPresent);
            if (blockItem) {
                Identifier registeredBlockId = blockId.orElseThrow(() ->
                        new IllegalArgumentException("Block item declaration " + id + " is missing its block"));
                Supplier<Block> block = () -> declaration.get().blockItem().orElseThrow().block().get();
                return items.registerBlockItem(BlockItemId.create(registeredBlockId, id), block,
                        (registeredBlock, itemProperties) -> declaration.get().blockItem().orElseThrow()
                                .factory().apply(registeredBlock, itemProperties), properties);
            }
            return items.register(id.getPath(),
                    itemProperties -> declaration.get().factory().apply(itemProperties), properties);
        });
    }

    public record Definition(
            Either<BlockItemDefinition, ExtensibleCodec.Entry<Function<Item.Properties, Item>>> factoryType,
            Map<DataComponentType<?>, Object> properties
    ) {
        public static Definition item(ExtensibleCodec.Entry<Function<Item.Properties, Item>> type,
                Map<DataComponentType<?>, Object> properties) {
            return new Definition(Either.right(type), properties);
        }

        public static Definition blockItem(Supplier<Block> block,
                ExtensibleCodec.Entry<BiFunction<Block, Item.Properties, Item>> type,
                Map<DataComponentType<?>, Object> properties) {
            return new Definition(Either.left(new BlockItemDefinition(block, type)), properties);
        }

        public ExtensibleCodec.Entry<Function<Item.Properties, Item>> type() {
            return factoryType.right().orElseThrow();
        }

        public Optional<BlockItemDefinition> blockItem() {
            return factoryType.left();
        }

        public boolean isBlockItem() {
            return blockItem().isPresent();
        }

        public Function<Item.Properties, Item> factory() {
            return type().get();
        }
    }

    public record BlockItemDefinition(
            Supplier<Block> block,
            ExtensibleCodec.Entry<BiFunction<Block, Item.Properties, Item>> type
    ) {
        public BiFunction<Block, Item.Properties, Item> factory() {
            return type.get();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Item.Properties createProperties(Map<DataComponentType<?>, Object> components) {
        Item.Properties properties = new Item.Properties();
        components.forEach((type, value) -> properties.component((DataComponentType<T>) type, (T) value));
        return properties;
    }
}
