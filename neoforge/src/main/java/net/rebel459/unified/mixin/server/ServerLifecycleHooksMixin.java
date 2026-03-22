package net.rebel459.unified.mixin.server;

import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.rebel459.unified.platform.NeoForgeHelpersImpl;
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