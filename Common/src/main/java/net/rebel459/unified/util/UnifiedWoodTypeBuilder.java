package net.rebel459.unified.util;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

public final class UnifiedWoodTypeBuilder {
    private SoundType soundGroup;
    private SoundType hangingSignSoundGroup;
    private SoundEvent fenceGateCloseSound;
    private SoundEvent fenceGateOpenSound;

    public UnifiedWoodTypeBuilder() {
        this.soundGroup = SoundType.WOOD;
        this.hangingSignSoundGroup = SoundType.HANGING_SIGN;
        this.fenceGateCloseSound = SoundEvents.FENCE_GATE_CLOSE;
        this.fenceGateOpenSound = SoundEvents.FENCE_GATE_OPEN;
    }

    public UnifiedWoodTypeBuilder soundGroup(SoundType soundGroup) {
        this.soundGroup = soundGroup;
        return this;
    }

    public UnifiedWoodTypeBuilder hangingSignSoundGroup(SoundType hangingSignSoundGroup) {
        this.hangingSignSoundGroup = hangingSignSoundGroup;
        return this;
    }

    public UnifiedWoodTypeBuilder fenceGateCloseSound(SoundEvent fenceGateCloseSound) {
        this.fenceGateCloseSound = fenceGateCloseSound;
        return this;
    }

    public UnifiedWoodTypeBuilder fenceGateOpenSound(SoundEvent fenceGateOpenSound) {
        this.fenceGateOpenSound = fenceGateOpenSound;
        return this;
    }

    public static UnifiedWoodTypeBuilder copyOf(UnifiedWoodTypeBuilder builder) {
        UnifiedWoodTypeBuilder copy = new UnifiedWoodTypeBuilder();
        copy.soundGroup(builder.soundGroup);
        copy.hangingSignSoundGroup(builder.hangingSignSoundGroup);
        copy.fenceGateCloseSound(builder.fenceGateCloseSound);
        copy.fenceGateOpenSound(builder.fenceGateOpenSound);
        return copy;
    }

    public static UnifiedWoodTypeBuilder copyOf(WoodType woodType) {
        UnifiedWoodTypeBuilder copy = new UnifiedWoodTypeBuilder();
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