package net.rebel459.unified.impl.data.helper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.codec.UnifiedCodecs;
import net.rebel459.unified.api.core.UnifiedEvents;
import net.rebel459.unified.api.core.UnifiedHelpers;

import java.util.Map;
import java.util.function.Predicate;

public class ComponentModifiers {

    public static final ResourceKey<Registry<Definition>> KEY = ResourceKey.createRegistryKey(Unified.id("component_modifiers"));

    public static void init() {
        UnifiedHelpers.DATA_REGISTRIES.register(KEY, UnifiedCodecs.loadRequirements(Definition.CODEC, () -> new Definition(ExtensibleCodecs.ITEM_PREDICATE.never().create(), Map.of())));
        UnifiedEvents.DefaultDataComponents.modify((item, builder, provider) -> {
            ItemStack defaultStack = item.getDefaultInstance();
            provider.lookup(KEY).ifPresent(modifiers -> modifiers.listElements().forEach(modifier -> {
                if (modifier.value().predicate().get().test(defaultStack)) {
                    applyComponents(builder, modifier.value().components());
                }
            }));
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> void applyComponents(DataComponentMap.Builder builder, Map<DataComponentType<?>, Object> components) {
        components.forEach((type, value) -> builder.set((DataComponentType<T>) type, (T) value));
    }

    public record Definition(ExtensibleCodec.Entry<Predicate<ItemStack>> predicate, Map<DataComponentType<?>, Object> components) {
        public static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExtensibleCodecs.ITEM_PREDICATE.codec().fieldOf("predicate").forGetter(Definition::predicate),
                DataComponentType.VALUE_MAP_CODEC.fieldOf("components").forGetter(Definition::components)
        ).apply(instance, Definition::new));
    }
}
