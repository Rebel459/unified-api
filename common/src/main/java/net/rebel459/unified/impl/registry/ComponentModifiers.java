package net.rebel459.unified.impl.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.core.UnifiedEvents;
import net.rebel459.unified.api.core.UnifiedHelpers;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ComponentModifiers {

    public static final ResourceKey<Registry<Modifier>> KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "component_modifiers"));

    public static void init() {
        UnifiedHelpers.DATA_REGISTRIES.register(KEY, Modifier.CODEC);
        UnifiedEvents.DefaultDataComponents.modify((item, builder, provider) -> {
                    provider.lookup(KEY).ifPresent(modifiers -> modifiers.listElements().forEach(modifier -> {
                                        ItemStack defaultStack = item.getDefaultInstance();
                                        for (Target target : modifier.value().targets()) {
                                            if (target.items().size() != 0 && !target.items().contains(defaultStack.typeHolder())) continue;
                                            if (target.components().entrySet().stream().allMatch(entry -> Objects.equals(defaultStack.get(entry.getKey()), entry.getValue()))) {
                                                applyComponents(builder, modifier.value().components());
                                                break;
                                            }
                                        }
                                    }
                            )
                    );
                }
        );
    }

    @SuppressWarnings("unchecked")
    private static <T> void applyComponents(DataComponentMap.Builder builder, Map<DataComponentType<?>, Object> components) {
        components.forEach((type, value) -> builder.set((DataComponentType<T>) type, (T) value));
    }

    public record Modifier(List<Target> targets, Map<DataComponentType<?>, Object> components) {
        public static final Codec<Modifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Target.CODEC.listOf().fieldOf("targets").forGetter(Modifier::targets),
                DataComponentType.VALUE_MAP_CODEC.fieldOf("components").forGetter(Modifier::components)
        ).apply(instance, Modifier::new));
    }

    public record Target(HolderSet<Item> items, Map<DataComponentType<?>, Object> components) {
        public static final Codec<Target> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("items", HolderSet.empty()).forGetter(Target::items),
                DataComponentType.VALUE_MAP_CODEC.optionalFieldOf("components", Map.of()).forGetter(Target::components)
        ).apply(instance, Target::new));
    }
}