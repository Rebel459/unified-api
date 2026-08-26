package net.rebel459.unified.util.data.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.rebel459.unified.Unified;
import net.rebel459.unified.platform.UnifiedRegistries;
import net.rebel459.unified.util.codec.ExtensibleCodec;
import net.rebel459.unified.util.codec.CodecUtils;
import net.rebel459.unified.util.registry.RegistryResourceListener;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class ItemRegistry extends RegistryResourceListener<ItemRegistry.Definition> {
    public static final ExtensibleCodec<Function<Item.Properties, Item>> TYPES = new ExtensibleCodec<>("type");

    public static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TYPES.codec(Identifier.withDefaultNamespace("item")).forGetter(Definition::type),
            CodecUtils.supplied(DataComponentType.VALUE_MAP_CODEC, () -> RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY), ItemRegistry::createProperties)
                    .optionalFieldOf("properties").xmap(properties -> properties.orElse(Item.Properties::new), Optional::of).forGetter(Definition::properties)
    ).apply(instance, Definition::new));

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Unified.MOD_ID, "items");

    public ItemRegistry() {
        super(ID, CODEC, BlockRegistry.ID, EntityTypeRegistry.ID);
    }

    @Override
    protected void register(Identifier id, Definition definition) {
        Supplier<Item.Properties> properties = definition.properties();
        Supplier<Item.Properties> copied = properties;
        if (definition.factory() instanceof BlockItem) {
            properties = () -> copied.get().useBlockDescriptionPrefix();
        }
        UnifiedRegistries.Items.create(id.getNamespace()).register(id.getPath(), definition.factory(), properties);
    }

    public record Definition(ExtensibleCodec.Entry<Function<Item.Properties, Item>> type, Supplier<Item.Properties> properties) {
        public Function<Item.Properties, Item> factory() {
            return type.value();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Item.Properties createProperties(Map<DataComponentType<?>, Object> components) {
        Item.Properties properties = new Item.Properties();
        components.forEach((type, value) -> properties.component((DataComponentType<T>) type, (T) value));
        return properties;
    }
}
