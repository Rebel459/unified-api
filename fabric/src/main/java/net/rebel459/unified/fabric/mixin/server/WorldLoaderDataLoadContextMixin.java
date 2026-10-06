package net.rebel459.unified.fabric.mixin.server;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.WorldDataConfiguration;
import net.rebel459.unified.fabric.core.FabricBiomeModifications;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldLoader.DataLoadContext.class)
public abstract class WorldLoaderDataLoadContextMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void applyBiomeModifications(ResourceManager resources, WorldDataConfiguration dataConfiguration, HolderLookup.Provider datapackWorldgen, RegistryAccess.Frozen datapackDimensions, CallbackInfo ci) {
        FabricBiomeModifications.apply(datapackWorldgen);
    }
}
