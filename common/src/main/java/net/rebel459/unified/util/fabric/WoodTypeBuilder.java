package net.rebel459.unified.util.fabric;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

public final class WoodTypeBuilder {
    private SoundType soundGroup;
    private SoundType hangingSignSoundGroup;
    private SoundEvent fenceGateCloseSound;
    private SoundEvent fenceGateOpenSound;

    public WoodTypeBuilder() {
        this.soundGroup = SoundType.WOOD;
        this.hangingSignSoundGroup = SoundType.HANGING_SIGN;
        this.fenceGateCloseSound = SoundEvents.FENCE_GATE_CLOSE;
        this.fenceGateOpenSound = SoundEvents.FENCE_GATE_OPEN;
    }

    public WoodTypeBuilder soundGroup(SoundType soundGroup) {
        this.soundGroup = soundGroup;
        return this;
    }

    public WoodTypeBuilder hangingSignSoundGroup(SoundType hangingSignSoundGroup) {
        this.hangingSignSoundGroup = hangingSignSoundGroup;
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
        copy.soundGroup(builder.soundGroup);
        copy.hangingSignSoundGroup(builder.hangingSignSoundGroup);
        copy.fenceGateCloseSound(builder.fenceGateCloseSound);
        copy.fenceGateOpenSound(builder.fenceGateOpenSound);
        return copy;
    }

    public static WoodTypeBuilder copyOf(WoodType woodType) {
        WoodTypeBuilder copy = new WoodTypeBuilder();
        copy.soundGroup(woodType.soundType());
        copy.hangingSignSoundGroup(woodType.hangingSignSoundType());
        copy.fenceGateCloseSound(woodType.fenceGateClose());
        copy.fenceGateOpenSound(woodType.fenceGateOpen());
        return copy;
    }

    public WoodType register(Identifier id, BlockSetType setType) {
        return WoodType.register(this.build(id, setType));
    }

    public WoodType build(Identifier id, BlockSetType setType) {
        return new WoodType(id.toString(), setType, this.soundGroup, this.hangingSignSoundGroup, this.fenceGateCloseSound, this.fenceGateOpenSound);
    }
}