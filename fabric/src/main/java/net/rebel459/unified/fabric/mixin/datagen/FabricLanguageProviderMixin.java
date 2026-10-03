package net.rebel459.unified.fabric.mixin.datagen;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import net.rebel459.unified.fabric.FabricUnifiedDatagen;
import net.rebel459.unified.fabric.datagen.AutomaticTranslations;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = FabricLanguageProvider.class, remap = false)
public abstract class FabricLanguageProviderMixin implements AutomaticTranslations {
    @Shadow @Final protected FabricPackOutput packOutput;
    @Shadow @Final private String languageCode;
    @Unique private boolean unified$automaticTranslations;

    @Override public void unified$enableTranslations() {
        unified$automaticTranslations = true;
    }

    @WrapOperation(method = "lambda$run$0", at = @At(value = "INVOKE", target =
            "Lnet/fabricmc/fabric/api/datagen/v1/provider/FabricLanguageProvider;generateTranslations(Lnet/minecraft/core/HolderLookup$Provider;Lnet/fabricmc/fabric/api/datagen/v1/provider/FabricLanguageProvider$TranslationBuilder;)V"))
    private void unified$addTranslations(FabricLanguageProvider provider, HolderLookup.Provider registries,
            FabricLanguageProvider.TranslationBuilder translations, Operation<Void> original) {
        if (unified$automaticTranslations) {
            FabricUnifiedDatagen.addTranslations(packOutput.getModId(), languageCode, translations);
        }
        original.call(provider, registries, translations);
    }
}
