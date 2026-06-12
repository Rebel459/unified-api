package net.rebel459.unified.util.fabric;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

public final class WoodTypeBuilder {
	private SoundType soundType = SoundType.WOOD;
	private SoundType hangingSignSoundType = SoundType.HANGING_SIGN;
	private SoundEvent fenceGateCloseSound = SoundEvents.FENCE_GATE_CLOSE;
	private SoundEvent fenceGateOpenSound = SoundEvents.FENCE_GATE_OPEN;

	public WoodTypeBuilder soundType(SoundType soundType) {
		this.soundType = soundType;
		return this;
	}

	public WoodTypeBuilder hangingSignSoundType(SoundType hangingSignSoundType) {
		this.hangingSignSoundType = hangingSignSoundType;
		return this;
	}

	public WoodTypeBuilder fenceGateCloseSound(SoundEvent fenceGateCloseSound) {
		this.fenceGateCloseSound = fenceGateCloseSound;
		return this;
	}

	public WoodTypeBuilder fenceGateOpenSound(SoundEvent fenceGateOpenSound) {
		this.fenceGateOpenSound = fenceGateOpenSound;
		return this;
	}

	public static WoodTypeBuilder copyOf(WoodTypeBuilder builder) {
		WoodTypeBuilder copy = new WoodTypeBuilder();
		copy.soundType(builder.soundType);
		copy.hangingSignSoundType(builder.hangingSignSoundType);
		copy.fenceGateCloseSound(builder.fenceGateCloseSound);
		copy.fenceGateOpenSound(builder.fenceGateOpenSound);
		return copy;
	}

	public static WoodTypeBuilder copyOf(WoodType woodType) {
		WoodTypeBuilder copy = new WoodTypeBuilder();
		copy.soundType(woodType.soundType());
		copy.hangingSignSoundType(woodType.hangingSignSoundType());
		copy.fenceGateCloseSound(woodType.fenceGateClose());
		copy.fenceGateOpenSound(woodType.fenceGateOpen());
		return copy;
	}

	public WoodType register(Identifier id, BlockSetType setType) {
		return WoodType.register(this.build(id, setType));
	}

	public WoodType build(Identifier id, BlockSetType setType) {
		return new WoodType(id.toString(), setType,
				soundType,
				hangingSignSoundType, fenceGateCloseSound, fenceGateOpenSound);
	}
}