package net.rebel459.unified.api.data.helper;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.rebel459.unified.api.codec.CodecGenerator;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.impl.data.helper.ComponentModifiers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class ComponentModifierGenerator extends HelperGenerator {

    public ComponentModifierGenerator(String modId, String namespace, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
        super(modId, namespace, requirement);
    }

    public Builder createAndRegister(String name, Function<HolderLookup.Provider, Predicate<ItemStack>> predicate) {
        return create(name, provider -> ExtensibleCodecs.ITEM_PREDICATE.register(Identifier.fromNamespaceAndPath(namespace, "component_modifier/" + name), () -> predicate.apply(provider)).create());
    }

    public Builder create(String name, Function<HolderLookup.Provider, ExtensibleCodec.Entry<Predicate<ItemStack>>> predicate) {
        return new Builder(name, predicate, modId, namespace, requirement);
    }

    public static final class Builder extends HelperGenerator.Builder {

        private final Map<DataComponentType<?>, Function<HolderLookup.Provider, ?>> components = new HashMap<>();

        Builder(String name, Function<HolderLookup.Provider, ExtensibleCodec.Entry<Predicate<ItemStack>>> predicate, String modId, String namespace, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
            super(name, modId, namespace, requirement);
            CodecGenerator.data(modId, Identifier.fromNamespaceAndPath(namespace, "unified/component_modifiers/" + name), requirement, (provider, ops) -> {
                var providedComponents = new HashMap<DataComponentType<?>, Object>();
                for (DataComponentType<?> component : Map.copyOf(components).keySet()) {
                    Object value = components.get(component).apply(provider);
                    providedComponents.put(component, value);
                }
                return ComponentModifiers.Definition.CODEC.encodeStart(ops, new ComponentModifiers.Definition(predicate.apply(provider), Map.copyOf(providedComponents))).getOrThrow();
            });
        }

        public <T> ComponentModifierGenerator.Builder set(DataComponentType<T> type, T value) {
            components.put(type, _ -> value);
            return this;
        }

        public <T> ComponentModifierGenerator.Builder setDelayed(DataComponentType<T> type, Function<HolderLookup.Provider, T> value) {
            components.put(type, value);
            return this;
        }
    }
}
