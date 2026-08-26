package net.rebel459.unified.mixin.server;

import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.rebel459.unified.platform.NeoForgeHelpersImpl;
import net.rebel459.unified.util.neoforge.BiomeBuilderEvent;
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
        NeoForgeHelpersImpl.BiomeModifications.MODIFIERS.clear();
        BiomeBuilderEvent.passOnRunModifiers(server.reloadableRegistries().lookup());
        List<BiomeModifier> modifiers = new ArrayList<>(biomeModifiers);
        modifiers.addAll(NeoForgeHelpersImpl.BiomeModifications.MODIFIERS);
        return modifiers;
    }
}