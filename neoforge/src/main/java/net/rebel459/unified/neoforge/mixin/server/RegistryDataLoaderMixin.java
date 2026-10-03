package net.rebel459.unified.neoforge.mixin.server;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.packs.resources.ResourceManager;
import net.rebel459.unified.neoforge.core.NeoForgeUnifiedEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

@Mixin(RegistryDataLoader.class)
public abstract class RegistryDataLoaderMixin {

    @Inject(
            method = "load(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/List;)Ljava/util/concurrent/CompletableFuture;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void modifyLootTables(
            ResourceManager resourceManager,
            List<HolderLookup.RegistryLookup<?>> contextRegistries,
            List<RegistryDataLoader.RegistryData<?>> registriesToLoad,
            Executor executor,
            List<Registry.PendingTags<?>> pendingTags,
            CallbackInfoReturnable<CompletableFuture<RegistryAccess.Frozen>> cir
    ) {
        cir.setReturnValue(cir.getReturnValue().thenApply(loaded -> {
            loaded.lookup(Registries.LOOT_TABLE).ifPresent(tables -> {
                HolderLookup.Provider provider = HolderLookup.Provider.create(
                        Stream.concat(contextRegistries.stream(), loaded.listRegistries())
                );
                tables.listElements().forEach(holder ->
                        NeoForgeUnifiedEvents.modifyLootTable(holder.key(), holder.value(), provider));
            });
            return loaded;
        }));
    }
}
