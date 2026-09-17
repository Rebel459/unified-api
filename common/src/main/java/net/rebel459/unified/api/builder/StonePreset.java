package net.rebel459.unified.api.builder;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;

import java.util.Optional;

public final class StonePreset {

    final StoneSet.Settings settings;

    StonePreset(StoneSet.Settings settings) {
        this.settings = settings;
    }

    public static final StonePreset DEFAULT = new StoneSet.PresetBuilder()
            .build();

    public static final StonePreset BASIC = create()
            .hasWall(false)
            .build();

    public static final StonePreset LEGACY = create()
            .hasLegacySlab(true)
            .setDestroyTime(2F)
            .build();

    public static final StonePreset STONE = createFrom(BlockSetType.STONE)
            .hasLegacySlab(true)
            .hasButton(true)
            .hasPressurePlate(true)
            .hasWall(false)
            .build();

    public static final StonePreset STONE_BRICKS = create()
            .hasLegacySlab(true)
            .hasCracked(true)
            .hasChiseled(true)
            .build();

    public static final StonePreset COBBLED_DEEPSLATE = create()
            .setDestroyTime(3.5F)
            .setExplosionResistance(6F)
            .setSoundType(() -> SoundType.DEEPSLATE)
            .build();

    public static final StonePreset POLISHED_DEEPSLATE = createFrom(COBBLED_DEEPSLATE)
            .setSoundType(() -> SoundType.POLISHED_DEEPSLATE)
            .build();

    public static final StonePreset DEEPSLATE_BRICKS = createFrom(COBBLED_DEEPSLATE)
            .setSoundType(() -> SoundType.DEEPSLATE_BRICKS)
            .hasCracked(true)
            .build();

    public static final StonePreset DEEPSLATE_TILES = createFrom(DEEPSLATE_BRICKS)
            .setSoundType(() -> SoundType.DEEPSLATE_TILES)
            .build();

    public static final StonePreset TUFF = create()
            .setSoundType(() -> SoundType.TUFF)
            .hasChiseled(true)
            .build();

    public static final StonePreset POLISHED_TUFF = create()
            .setSoundType(() -> SoundType.POLISHED_TUFF)
            .build();

    public static final StonePreset TUFF_BRICKS = create()
            .setSoundType(() -> SoundType.TUFF_BRICKS)
            .hasChiseled(true)
            .build();

    public static final StonePreset MUD_BRICKS = create()
            .setSoundType(() -> SoundType.MUD_BRICKS)
            .setExplosionResistance(3F)
            .build();

    public static final StonePreset RESIN_BRICKS = create()
            .setSoundType(() -> SoundType.RESIN_BRICKS)
            .build();

    public static final StonePreset SANDSTONE = create()
            .hasLegacySlab(true)
            .setDestroyTime(0.8F)
            .setExplosionResistance(0.8F)
            .build();

    public static final StonePreset NETHER_BRICKS = create()
            .setSoundType(() -> SoundType.NETHER_BRICKS)
            .alternateFenceRecipe(() -> Items.NETHER_BRICK, 6)
            .hasLegacySlab(true)
            .hasCracked(true)
            .hasFence(true)
            .hasChiseled(true)
            .build();

    public static final StonePreset POLISHED_BLACKSTONE = createFrom(BlockSetType.POLISHED_BLACKSTONE)
            .hasLegacySlab(true)
            .hasButton(true)
            .hasPressurePlate(true)
            .hasChiseled(true)
            .build();

    public static final StonePreset END_STONE_BRICKS = create()
            .setDestroyTime(3F)
            .setExplosionResistance(9F)
            .build();

    public static final StonePreset PURPUR = createFrom(StonePreset.BASIC)
            .hasLegacySlab(true)
            .hasPillar(true)
            .baseBlockSuffix("block")
            .build();

    public static final StonePreset SULFUR = create()
            .setSoundType(() -> SoundType.SULFUR)
            .hasChiseled(true)
            .build();

    public static final StonePreset POLISHED_SULFUR = createFrom(SULFUR)
            .hasChiseled(false)
            .build();

    public static final StonePreset CINNABAR = create()
            .setSoundType(() -> SoundType.CINNABAR)
            .hasChiseled(true)
            .build();

    public static final StonePreset POLISHED_CINNABAR = createFrom(CINNABAR)
            .hasChiseled(false)
            .build();

    public static StoneSet.PresetBuilder create() {
        return createFrom(DEFAULT);
    }

    public static StoneSet.PresetBuilder createFrom(StonePreset preset) {
        return new StoneSet.PresetBuilder(preset.settings.copy());
    }

    public static StoneSet.PresetBuilder createFrom(BlockSetType blockSetType) {
        return create()
                .setButtonSounds(blockSetType::buttonClickOn, blockSetType::buttonClickOff)
                .setPressurePlateSounds(blockSetType::pressurePlateClickOn, blockSetType::pressurePlateClickOff)
                .setSoundType(blockSetType::soundType)
                .canArrowsActivateButton(blockSetType.canButtonBeActivatedByArrows())
                .setPressurePlateSensitivity(blockSetType.pressurePlateSensitivity());
    }
}