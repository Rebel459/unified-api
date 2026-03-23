package net.rebel459.unified.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.rebel459.unified.platform.UnifiedEvents;

import java.util.HashMap;
import java.util.List;

public class PersistentCooldowns {

	public static final Codec<List<Record>> CODEC = PersistentCooldowns.Record.CODEC.listOf();

	public static HashMap<ServerPlayer, List<Record>> PENDING_COOLDOWNS = new HashMap<>();

	public static int getCooldown(ServerPlayer player, Identifier id) {
		var cooldowns = player.getCooldowns();
		var cooldown = cooldowns.cooldowns.get(id);
		if (cooldown != null) {
			return cooldown.endTime() - cooldowns.tickCount;
		}
		return 0;
	}

	public static void init() {
		UnifiedEvents.Players.onJoin(player -> {
			if (!(player instanceof ServerPlayer serverPlayer)) return;
			List<Record> list = PENDING_COOLDOWNS.get(serverPlayer);
			if (list != null) {
				list.forEach(record -> player.getCooldowns().addCooldown(record.id, record.remainingCooldown));
			}
		});
	}

	public record Record(Identifier id, int remainingCooldown) {
		public static final Codec<PersistentCooldowns.Record> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Identifier.CODEC.fieldOf("cooldown_id").forGetter(PersistentCooldowns.Record::id),
				Codec.INT.fieldOf("cooldown_remaining").orElse(0).forGetter(PersistentCooldowns.Record::remainingCooldown)
		).apply(instance, PersistentCooldowns.Record::new));
	}
}