package net.rebel459.unified.mixin;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.rebel459.unified.UnifiedNeoForge;
import net.rebel459.unified.platform.HelpersImpl;
import net.rebel459.unified.platform.NeoForgeHelpersImpl;
import net.rebel459.unified.util.UnifiedBiomeModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = net.neoforged.neoforge.server.ServerLifecycleHooks.class, remap = false)
public class ServerLifecycleHooksMixin {

    @ModifyVariable(
            method = "runModifiers",
            at = @At("STORE"),
            ordinal = 0
    )
    private static List<BiomeModifier> lithostitched$injectBiomeModifers(List<BiomeModifier> biomeModifiers, MinecraftServer server) {
        List<BiomeModifier> modifiers = new ArrayList<>(biomeModifiers);
        modifiers.addAll(NeoForgeHelpersImpl.BiomeModifications.MODIFIERS);
        return modifiers;
    }
}