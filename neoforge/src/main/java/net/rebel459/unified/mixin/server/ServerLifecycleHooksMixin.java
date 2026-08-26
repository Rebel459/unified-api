package net.rebel459.unified.mixin.server;

import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.rebel459.unified.platform.NeoForgeBiomeModifications;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = ServerLifecycleHooks.class, remap = false)
public class ServerLifecycleHooksMixin {

    @ModifyVariable(
            method = "runModifiers",
            at = @At("STORE"),
            name = "biomeModifiers"
    )
    private static List<BiomeModifier> addBiomeModifiers(List<BiomeModifier> biomeModifiers, MinecraftServer server) {
        List<BiomeModifier> modifiers = new ArrayList<>(biomeModifiers);
        modifiers.addAll(NeoForgeBiomeModifications.create(server));
        return modifiers;
    }
}
