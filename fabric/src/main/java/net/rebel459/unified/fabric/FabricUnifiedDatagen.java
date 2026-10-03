package net.rebel459.unified.fabric;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider.TranslationBuilder;
import net.rebel459.unified.api.asset.BlockAsset;
import net.rebel459.unified.api.asset.ItemAsset;
import net.rebel459.unified.impl.core.DataProvider;
import net.rebel459.unified.impl.core.DataProviders;
import net.rebel459.unified.fabric.datagen.DatagenPack;
import net.rebel459.unified.fabric.datagen.FabricDatagenProvider;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class FabricUnifiedDatagen {
    /** Composes model providers and runs other Fabric providers in registration order. */
    public static final class Pack {
        private final DatagenPack providers;

        public Pack(FabricDataGenerator.Pack pack) {
            providers = pack.addProvider((output, registries) -> new DatagenPack(output, registries));
        }

        public <T extends net.minecraft.data.DataProvider> T addProvider(FabricDataGenerator.Pack.Factory<T> factory) {
            return providers.add(factory);
        }

        public <T extends net.minecraft.data.DataProvider> T addProvider(FabricDataGenerator.Pack.RegistryDependentFactory<T> factory) {
            return providers.add(factory);
        }
    }

    /**
     * Add Unified's entries inside a language provider registered outside Unified's pack.
     * Providers added to the pack returned by register already receive these automatically.
     */
    public static void addTranslations(String modId, String language, TranslationBuilder translations) {
        FabricDatagenProvider.LanguageProvider.addTranslations(modId, language, DataProviders.LANGUAGES, translations);
    }

    public static Pack register(FabricDataGenerator generator) {
        return FabricDatagenProvider.register(generator);
    }

    /**
     * Binds a common request channel to the Fabric providers which consume it.
     * Register bindings before calling {@link #register(FabricDataGenerator)}.
     */
    public static synchronized <T> void registerDataProvider(DataProvider<T> provider, ProviderFactory<T> factory) {
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(factory, "factory");
        if (FabricDatagenProvider.DATA_PROVIDER_ADAPTERS.putIfAbsent(provider, factory) != null) {
            throw new IllegalArgumentException("Duplicate Fabric data provider binding for " + provider.id());
        }
    }

    public static synchronized void registerBlockAsset(BlockAsset<Void> type, Consumer<FabricDatagenProvider.BlockModelContext> generator) {
        registerBlockAsset(type, (_, context) -> generator.accept(context));
    }
    public static synchronized <T> void registerBlockAsset(BlockAsset<T> type, BiConsumer<T, FabricDatagenProvider.BlockModelContext> generator) {
        FabricDatagenProvider.BlockAssetAdapter<T> adapter = generator::accept;
        if (FabricDatagenProvider.BLOCK_ASSET_ADAPTERS.putIfAbsent(type, adapter) != null) throw new IllegalArgumentException("Duplicate Fabric block asset binding for " + type.id());
    }
    public static synchronized void registerItemAsset(ItemAsset<Void> type, Consumer<FabricDatagenProvider.ItemModelContext> generator) {
        registerItemAsset(type, (_, context) -> generator.accept(context));
    }
    public static synchronized <T> void registerItemAsset(ItemAsset<T> type, BiConsumer<T, FabricDatagenProvider.ItemModelContext> generator) {
        FabricDatagenProvider.ItemAssetAdapter<T> adapter = generator::accept;
        if (FabricDatagenProvider.ITEM_ASSET_ADAPTERS.putIfAbsent(type, adapter) != null) throw new IllegalArgumentException("Duplicate Fabric item asset binding for " + type.id());
    }

    @FunctionalInterface
    public interface ProviderFactory<T> {
        void register(Pack pack, String modId, DataProvider<T> requests);
    }
}
