package net.rebel459.unified.fabric.datagen;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class DatagenPack implements DataProvider {
    private final FabricPackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;
    private final ModelGroup models;
    private final List<DataProvider> providers = new ArrayList<>();
    private boolean started;

    public DatagenPack(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.output = output;
        this.registries = registries;
        models = new ModelGroup(output);
    }

    public <T extends DataProvider> T add(FabricDataGenerator.Pack.Factory<T> factory) {
        return add(factory.create(output));
    }

    public <T extends DataProvider> T add(FabricDataGenerator.Pack.RegistryDependentFactory<T> factory) {
        return add(factory.create(output, registries));
    }

    private <T extends DataProvider> T add(T provider) {
        if (started) throw new IllegalStateException("Cannot add providers after datagen starts");
        Objects.requireNonNull(provider);
        if (provider instanceof FabricModelProvider model) {
            requireStandardRun(provider, ModelProvider.class);
            models.contributions.add(model);
        } else {
            if (provider instanceof FabricLanguageProvider language
                    && !(provider instanceof FabricDatagenProvider.LanguageProvider)
                    && providers.stream().anyMatch(FabricDatagenProvider.LanguageProvider.class::isInstance)) {
                requireStandardRun(language, FabricLanguageProvider.class);
                ((AutomaticTranslations) language).unified$enableTranslations();
            }
            providers.add(provider);
        }
        return provider;
    }

    private static void requireStandardRun(DataProvider provider, Class<?> owner) {
        try {
            if (provider.getClass().getMethod("run", CachedOutput.class).getDeclaringClass() != owner) {
                throw new IllegalArgumentException("Compose generation callbacks rather than overriding run(CachedOutput): "
                        + provider.getClass().getName());
            }
        } catch (NoSuchMethodException impossible) { throw new AssertionError(impossible); }
    }

    @Override public String getName() { return "Unified data for " + output.getModId(); }

    @Override public CompletableFuture<?> run(CachedOutput cache) {
        started = true;
        CompletableFuture<?> result = models.contributions.isEmpty()
                ? CompletableFuture.completedFuture(null) : models.run(cache);
        var written = ConcurrentHashMap.<Path>newKeySet();
        CachedOutput explicitOutput = (path, bytes, hash) -> {
            cache.writeIfNeeded(path, bytes, hash);
            written.add(path);
        };
        for (DataProvider provider : providers) {
            if (!(provider instanceof FabricDatagenProvider.LanguageProvider)) {
                result = result.thenCompose(_ -> provider.run(explicitOutput));
            }
        }
        CachedOutput defaults = (path, bytes, hash) -> {
            if (!written.contains(path)) cache.writeIfNeeded(path, bytes, hash);
        };
        for (DataProvider provider : providers) {
            if (provider instanceof FabricDatagenProvider.LanguageProvider) {
                result = result.thenCompose(_ -> provider.run(defaults));
            }
        }
        return result;
    }
}
