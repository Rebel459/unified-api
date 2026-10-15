package net.rebel459.unified.impl.data.registry;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.google.gson.JsonElement;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.rebel459.unified.impl.util.BlockItemId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.codec.UnifiedCodecs;
import net.rebel459.unified.api.core.StagedRegistry;
import net.rebel459.unified.api.core.RegistryResourceListener;
import net.rebel459.unified.api.core.SuppliedItem;
import net.rebel459.unified.api.core.UnifiedRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class ItemRegistry extends RegistryResourceListener<ItemRegistry.Definition> {
    private static final MapCodec<Optional<Identifier>> BLOCK_ID_CODEC = Identifier.CODEC.optionalFieldOf("block");
    private static final MapCodec<Optional<Identifier>> TYPE_ID_CODEC = Identifier.CODEC.optionalFieldOf("type");
    private static final MapCodec<Map<DataComponentType<?>, Object>> PROPERTIES_CODEC =
            Codec.of(DataComponentType.VALUE_MAP_CODEC, new Decoder<>() {
                @Override
                public <O> DataResult<Pair<Map<DataComponentType<?>, Object>, O>> decode(DynamicOps<O> ops, O input) {
                    return ops.getMap(input).flatMap(map -> {
                        DataResult<Map<DataComponentType<?>, Object>> components = DataResult.success(new HashMap<>());
                        for (Pair<O, O> entry : map.entries().toList()) {
                            components = components.flatMap(values -> DataComponentType.CODEC.parse(ops, entry.getFirst())
                                    .map(type -> {
                                        values.put(type, new DeferredComponent(ops.convertTo(JsonOps.INSTANCE, entry.getSecond())));
                                        return values;
                                    }));
                        }
                        return components.map(values -> Pair.of(Map.copyOf(values), ops.empty()));
                    });
                }
            }).optionalFieldOf("properties")
                    .xmap(properties -> properties.orElse(Map.of()),
                            properties -> properties.isEmpty() ? Optional.empty() : Optional.of(properties));
    private static final Codec<Definition> ITEM_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtensibleCodecs.ITEM.mapCodec().forGetter(definition -> definition.factoryType.right().orElseThrow()),
            PROPERTIES_CODEC.forGetter(Definition::properties)
    ).apply(instance, Definition::item));
    private static final Codec<Definition> BLOCK_ITEM_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UnifiedCodecs.supplied(BuiltInRegistries.BLOCK).fieldOf("block").forGetter(definition -> definition.factoryType.left().orElseThrow().block()),
            ExtensibleCodecs.BLOCK_ITEM.mapCodec().forGetter(definition -> definition.factoryType.left().orElseThrow().type()),
            PROPERTIES_CODEC.forGetter(Definition::properties)
    ).apply(instance, Definition::blockItem));
    public static final Codec<Definition> CODEC = Codec.either(BLOCK_ITEM_CODEC, ITEM_CODEC).xmap(
            value -> value.map(Function.identity(), Function.identity()),
            definition -> definition.factoryType.left().isPresent() ? Either.left(definition) : Either.right(definition)
    );

    public static final Identifier ID = Unified.id("items");

    public ItemRegistry() {
        super(ID, CODEC, Registries.ITEM, EntityRegistry.ID);
    }

    @Override
    protected void register(Identifier id, DeferredDeclaration<Definition> declaration) {
        StagedRegistry.register(Registries.ITEM, id, () -> {
            Optional<Identifier> blockId = declaration.decode(BLOCK_ID_CODEC);
            Optional<Identifier> typeId = declaration.decode(TYPE_ID_CODEC);
            boolean blockItem = typeId.map(ExtensibleCodecs.BLOCK_ITEM::contains).orElseGet(blockId::isPresent);
            if (blockItem) {
                Identifier registeredBlockId = blockId.orElseThrow(() -> new IllegalArgumentException("Block item declaration " + id + " is missing its block"));
                return registerBlockItem(BlockItemId.create(registeredBlockId, id), declaration,
                        () -> createProperties(declaration.get().properties()));
            }
            return registerDefinition(id, declaration, () -> createProperties(declaration.get().properties()));
        });
    }

    public static SuppliedItem registerDefinition(Identifier id, Supplier<Definition> suppliedDefinition, Supplier<Item.Properties> properties) {
        UnifiedRegistries.Items items = UnifiedRegistries.Items.create(id.getNamespace());
        return items.register(id.getPath(), itemProperties -> suppliedDefinition.get().factoryType.right().orElseThrow().get().apply(itemProperties), properties);
    }

    public static SuppliedItem registerBlockItem(BlockItemId id, Supplier<Definition> suppliedDefinition, Supplier<Item.Properties> properties) {
        UnifiedRegistries.Items items = UnifiedRegistries.Items.create(id.item().identifier().getNamespace());
        Supplier<Block> block = () -> suppliedDefinition.get().factoryType.left().orElseThrow().block().get();
        return items.registerBlockItem(id, block, (registeredBlock, itemProperties) -> suppliedDefinition.get().factoryType.left().orElseThrow().factory().apply(registeredBlock, itemProperties), properties);
    }

    public record Definition(Either<BlockItemDefinition, ExtensibleCodec.Entry<Function<Item.Properties, Item>>> factoryType, Map<DataComponentType<?>, Object> properties) {
        public static Definition item(ExtensibleCodec.Entry<Function<Item.Properties, Item>> type, Map<DataComponentType<?>, Object> properties) {
            return new Definition(Either.right(type), properties);
        }

        public static Definition blockItem(Supplier<Block> block, ExtensibleCodec.Entry<BiFunction<Block, Item.Properties, Item>> type, Map<DataComponentType<?>, Object> properties) {
            return new Definition(Either.left(new BlockItemDefinition(block, type)), properties);
        }
    }

    public record BlockItemDefinition(Supplier<Block> block, ExtensibleCodec.Entry<BiFunction<Block, Item.Properties, Item>> type) {
        public BiFunction<Block, Item.Properties, Item> factory() {
            return type.get();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Item.Properties createProperties(Map<DataComponentType<?>, Object> components) {
        Item.Properties properties = new Item.Properties();
        components.forEach((type, value) -> {
            if (value instanceof DeferredComponent deferred) {
                properties.delayedComponent((DataComponentType<T>) type, provider -> decodeComponent(type, deferred, provider));
            } else {
                properties.component((DataComponentType<T>) type, (T) value);
            }
        });
        return properties;
    }

    @SuppressWarnings("unchecked")
    private static <T> T decodeComponent(DataComponentType<?> type, DeferredComponent component, HolderLookup.Provider provider) {
        return (T) type.codecOrThrow().parse(RegistryOps.create(JsonOps.INSTANCE, provider), component.value())
                .getOrThrow(error -> new IllegalArgumentException("Failed to decode item component " + type + ": " + error));
    }

    private record DeferredComponent(JsonElement value) {}
}
