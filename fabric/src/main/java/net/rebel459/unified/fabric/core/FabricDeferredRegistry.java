package net.rebel459.unified.platform;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.rebel459.unified.util.registry.Supplied;

import java.util.function.Supplier;

/** Fabric's immediate implementation of the general-purpose registry adapter. */
record FabricDeferredRegistry<Y>(String modId, Registry<Y> registry) implements UnifiedRegistries.DeferredRegistry<Y> {
    @Override
    public <T extends Y> Supplied<T> register(String path, Supplier<T> value) {
        ResourceKey<Y> key = ResourceKey.create(registry.key(), Identifier.fromNamespaceAndPath(modId, path));
        T registered = Registry.register(registry, key, value.get());
        return new Supplied<>(() -> registry, key, () -> registered);
    }

    @Override
    public <T extends Y> Holder<T> registerForHolder(String path, Supplier<T> value) {
        return Registry.registerForHolder(registry, Identifier.fromNamespaceAndPath(modId, path), value.get());
    }

    @Override
    @Deprecated
    public <T extends Y> Holder<T> registerHolder(String path, Supplier<T> value) {
        return registerForHolder(path, value);
    }

    @Override
    public void addAlias(Identifier convertedFrom, Identifier convertedTo) {
        registry.addAlias(convertedFrom, convertedTo);
    }
}
