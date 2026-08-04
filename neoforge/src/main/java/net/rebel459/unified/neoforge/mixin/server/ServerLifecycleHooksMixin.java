package net.rebel459.unified.neoforge.mixin.server;

import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.rebel459.unified.neoforge.core.NeoForgeHelpers;
import net.rebel459.unified.neoforge.util.BiomeBuilderEvent;
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
    private static List<BiomeModifier> addBiomeModifiers(List<BiomeModifier> biomeModifiers, MinecraftServer server) {
        NeoForgeHelpers.BiomeModifications.MODIFIERS.clear();
        BiomeBuilderEvent.passOnRunModifiers(server.reloadableRegistries().lookup());
        List<BiomeModifier> modifiers = new ArrayList<>(biomeModifiers);
        modifiers.addAll(NeoForgeHelpers.BiomeModifications.MODIFIERS);
        return modifiers;
    }
}