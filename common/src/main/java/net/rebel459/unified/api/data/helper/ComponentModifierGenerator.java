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
    public ComponentModifierGenerator(String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
        super(modId, requirement);
    }

    public Builder createAndRegister(String name, Function<HolderLookup.Provider, Predicate<ItemStack>> predicate) {
        return create(name, provider -> ExtensibleCodecs.ITEM_PREDICATES.register(Identifier.fromNamespaceAndPath(modId, "component_modifier/" + name), () -> predicate.apply(provider)).create());
    }

    public Builder create(String name, Function<HolderLookup.Provider, ExtensibleCodec.Entry<Predicate<ItemStack>>> predicate) {
        return new Builder(name, predicate, modId, requirement);
    }

    public static final class Builder extends HelperGenerator.Builder {

        private final Map<DataComponentType<?>, Object> components = new HashMap<>();

        Builder(String name, Function<HolderLookup.Provider, ExtensibleCodec.Entry<Predicate<ItemStack>>> predicate, String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
            super(name, modId, requirement);
            CodecGenerator.data(modId, Identifier.fromNamespaceAndPath(modId, "unified/component_modifiers/" + name), requirement, (provider, ops) ->
                    ComponentModifiers.Definition.CODEC.encodeStart(ops, new ComponentModifiers.Definition(predicate.apply(provider), Map.copyOf(components))).getOrThrow());
        }

        public <T> Builder set(DataComponentType<T> type, T value) {
            components.put(type, value);
            return this;
        }
    }
}
