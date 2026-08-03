package net.rebel459.unified.impl.mixin.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.tag.UnifiedItemTags;
import net.rebel459.unified.impl.tag.PersistentCooldowns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void addCooldowns(ValueOutput output, CallbackInfo ci) {
        ServerPlayer player = ServerPlayer.class.cast(this);

        List<PersistentCooldowns.Record> list = player.getCooldowns().cooldowns.keySet().stream()
                .filter(id -> BuiltInRegistries.ITEM.getOptional(id)
                        .map(item -> item.getDefaultInstance().is(UnifiedItemTags.PERSISTENT_COOLDOWNS))
                        .orElse(false))
                .map(identifier -> new PersistentCooldowns.Record(
                        identifier,
                        PersistentCooldowns.getCooldown(player, identifier)
                ))
                .toList();

        output.store(
                Identifier.fromNamespaceAndPath(Unified.MOD_ID, "persistent_cooldowns").toString(),
                PersistentCooldowns.CODEC,
                list
        );
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readCooldowns(ValueInput input, CallbackInfo ci) {
        ServerPlayer player = ServerPlayer.class.cast(this);

        input.read(
                Identifier.fromNamespaceAndPath(Unified.MOD_ID, "persistent_cooldowns").toString(),
                PersistentCooldowns.CODEC
        ).ifPresent(list -> PersistentCooldowns.PENDING_COOLDOWNS.put(player, list));
    }
}