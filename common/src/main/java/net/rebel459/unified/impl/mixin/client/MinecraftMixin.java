package net.rebel459.unified.impl.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeProbe;
import net.rebel459.unified.api.client.helper.ClientStructureReceiver;
import net.rebel459.unified.impl.client.core.CommonClientEvents;
import net.rebel459.unified.impl.client.helper.StructureMusicImpl;
import net.rebel459.unified.impl.core.CommonEvents;
import net.rebel459.unified.impl.network.StructurePacketImpl;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Shadow
    @Nullable
    public ClientLevel level;

    @WrapOperation(method = "getSituationalMusic", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/attribute/EnvironmentAttributeProbe;getValue(Lnet/minecraft/world/attribute/EnvironmentAttribute;F)Ljava/lang/Object;"))
    private <Value> Value getStructureMusic(EnvironmentAttributeProbe instance, EnvironmentAttribute<Value> attribute, float partialTicks, Operation<Value> original) {
        Value music;

        if (!StructureMusicImpl.STRUCTURE_MUSIC.isEmpty()) {
            if ((music = findMusic(ClientStructureReceiver.getPieceStructures(), StructureMusicImpl.STRUCTURE_MUSIC, false)) != null) return music;
            if ((music = findMusic(ClientStructureReceiver.getBoxStructures(), StructureMusicImpl.STRUCTURE_MUSIC, true)) != null) return music;
        }
        if (!StructureMusicImpl.STRUCTURE_TAG_MUSIC.isEmpty()) {
            if ((music = findMusic(ClientStructureReceiver.getPieceStructureTags(), StructureMusicImpl.STRUCTURE_TAG_MUSIC, false)) != null) return music;
            if ((music = findMusic(ClientStructureReceiver.getBoxStructureTags(), StructureMusicImpl.STRUCTURE_TAG_MUSIC, true)) != null) return music;
        }

        return original.call(instance, attribute, partialTicks);
    }

    @Unique
    @SuppressWarnings("unchecked")
    private static <K, Value> Value findMusic(Iterable<K> keys, Map<K, StructureMusicImpl.MusicAndRequirement> musicMap, boolean box) {
        for (K key : keys) {
            StructureMusicImpl.MusicAndRequirement music = musicMap.get(key);
            if (music != null && music.useFulLBox() == box) return (Value) music.music();
        }
        return null;
    }

    @Inject(method = "setLevel", at = @At(value = "HEAD"))
    private void stopClientLevel(ClientLevel level, CallbackInfo ci) {
        if (this.level != null) {
            CommonClientEvents.Instance.passOnLevelUnload(this.level);
            CommonEvents.Levels.passOnUnload(this.level);
        }
    }

    @WrapOperation(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;onDisconnected()V"))
    private void stopClientLevel(Gui gui, Operation<Void> original) {
        StructurePacketImpl.resetClientStructures();
        if (this.level != null) {
            CommonClientEvents.Instance.passOnLevelUnload(this.level);
            CommonEvents.Levels.passOnUnload(this.level);
        }
        original.call(gui);
    }
}
