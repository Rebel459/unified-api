package net.rebel459.unified.api.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;

import java.util.function.Consumer;
import java.util.function.Function;

public class UnifiedUseContextCodecs {

    private static ExtensibleCodec.Simple<Consumer<UseOnContext>> simple(String path, Consumer<UseOnContext> context) {
        return ExtensibleCodecs.USE_CONTEXT.register(Unified.id(path), () -> context);
    }

    private static <T> ExtensibleCodec.Complex<Consumer<UseOnContext>, T> complex(String path, MapCodec<T> codec, Function<T, Consumer<UseOnContext>> context) {
        return ExtensibleCodecs.USE_CONTEXT.register(Unified.id(path), codec, context);
    }

    public static final ExtensibleCodec.Complex<Consumer<UseOnContext>, SoundEvent> PLAY_SOUND = complex(
            "play_sound",
            BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("sound"),
            definition -> context -> {
                Player player = context.getPlayer();
                if (player == null) return;
                context.getLevel().playSound(player, context.getClickedPos(), definition, SoundSource.BLOCKS);
            }
    );

    public static final ExtensibleCodec.Complex<Consumer<UseOnContext>, Integer> DAMAGE_ITEM = complex(
            "damage_item",
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("amount", 1),
            definition -> context -> {
                Player player = context.getPlayer();
                if (player == null) return;
                context.getItemInHand().hurtAndBreak(definition, player, player.getEquipmentSlotForItem(context.getItemInHand()));
            }
    );

    public static void init() {}
}

