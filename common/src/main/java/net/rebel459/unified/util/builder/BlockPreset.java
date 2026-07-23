package net.rebel459.unified.util.builder;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

import java.util.Optional;

public final class BlockPreset {

    final BlockSet.Settings settings;

    BlockPreset(BlockSet.Settings settings) {
        this.settings = settings;
    }

    public static final BlockPreset DEFAULT = new BlockSet.PresetBuilder()
            .build();

    public static final BlockPreset BASIC = create()
            .hasWall(false)
            .build();

    public static final BlockPreset LEGACY = create()
            .hasLegacySlab(true)
            .setDestroyTime(2F)
            .build();

    public static final BlockPreset STONE = createFrom(BlockSetType.STONE)
            .hasLegacySlab(true)
            .hasButton(true)
            .hasPressurePlate(true)
            .hasWall(false)
            .build();

    public static final BlockPreset STONE_BRICKS = create()
            .hasLegacySlab(true)
            .hasCracked(true)
            .hasChiseled(true)
            .build();

    public static final BlockPreset COBBLED_DEEPSLATE = create()
            .setDestroyTime(3.5F)
            .setExplosionResistance(6F)
            .setSoundType(() -> SoundType.DEEPSLATE)
            .build();

    public static final BlockPreset POLISHED_DEEPSLATE = createFrom(COBBLED_DEEPSLATE)
            .setSoundType(() -> SoundType.POLISHED_DEEPSLATE)
            .build();

    public static final BlockPreset DEEPSLATE_BRICKS = createFrom(COBBLED_DEEPSLATE)
            .setSoundType(() -> SoundType.DEEPSLATE_BRICKS)
            .hasCracked(true)
            .build();

    public static final BlockPreset DEEPSLATE_TILES = createFrom(DEEPSLATE_BRICKS)
            .setSoundType(() -> SoundType.DEEPSLATE_TILES)
            .build();

    public static final BlockPreset TUFF = create()
            .setSoundType(() -> SoundType.TUFF)
            .hasChiseled(true)
            .build();

    public static final BlockPreset POLISHED_TUFF = create()
            .setSoundType(() -> SoundType.POLISHED_TUFF)
            .build();

    public static final BlockPreset TUFF_BRICKS = create()
            .setSoundType(() -> SoundType.TUFF_BRICKS)
            .hasChiseled(true)
            .build();

    public static final BlockPreset MUD_BRICKS = create()
            .setSoundType(() -> SoundType.MUD_BRICKS)
            .setExplosionResistance(3F)
            .build();

    public static final BlockPreset RESIN_BRICKS = create()
            .setSoundType(() -> SoundType.RESIN_BRICKS)
            .build();

    public static final BlockPreset SANDSTONE = create()
            .hasLegacySlab(true)
            .setDestroyTime(0.8F)
            .setExplosionResistance(0.8F)
            .build();

    public static final BlockPreset NETHER_BRICKS = create()
            .setSoundType(() -> SoundType.NETHER_BRICKS)
            .hasLegacySlab(true)
            .hasCracked(true)
            .hasFence(true)
            .hasChiseled(true)
            .build();

    public static final BlockPreset POLISHED_BLACKSTONE = createFrom(BlockSetType.POLISHED_BLACKSTONE)
            .hasLegacySlab(true)
            .hasButton(true)
            .hasPressurePlate(true)
            .hasChiseled(true)
            .build();

    public static final BlockPreset END_STONE_BRICKS = create()
            .setDestroyTime(3F)
            .setExplosionResistance(9F)
            .build();

    public static final BlockPreset PURPUR = createFrom(BlockPreset.BASIC)
            .hasLegacySlab(true)
            .hasPillar(true)
            .baseBlockSuffix(Optional.of("block"))
            .build();

    @Deprecated
    public static final BlockPreset CRACKED = create()
            .hasCracked(true)
            .build();

    @Deprecated
    public static final BlockPreset CHISELED = create()
            .hasChiseled(true)
            .build();

    public static BlockSet.PresetBuilder create() {
        return createFrom(DEFAULT);
    }

    public static BlockSet.PresetBuilder createFrom(BlockPreset preset) {
        return new BlockSet.PresetBuilder(preset.settings.copy());
    }

    public static BlockSet.PresetBuilder createFrom(BlockSetType blockSetType) {
        return create()
                .setButtonSounds(blockSetType::buttonClickOn, blockSetType::buttonClickOff)
                .setPressurePlateSounds(blockSetType::pressurePlateClickOn, blockSetType::pressurePlateClickOff)
                .setSoundType(blockSetType::soundType)
                .canArrowsActivateButton(blockSetType.canButtonBeActivatedByArrows())
                .setPressurePlateSensitivity(blockSetType.pressurePlateSensitivity());
    }
}