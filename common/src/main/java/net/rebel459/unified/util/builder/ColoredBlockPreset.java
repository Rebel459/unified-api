package net.rebel459.unified.util.builder;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;

import java.util.Optional;

public final class ColoredBlockPreset {

    final ColoredBlockSet.Settings settings;

    ColoredBlockPreset(ColoredBlockSet.Settings settings) {
        this.settings = settings;
    }

    public static final ColoredBlockPreset DEFAULT = new ColoredBlockSet.PresetBuilder()
            .build();

    public static final ColoredBlockPreset WOOL = create()
            .properties(() -> BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL))
            .setFlammability(30, 60)
            .build();

    public static final ColoredBlockPreset TERRACOTTA = create()
            .properties(() -> BlockBehaviour.Properties.ofFullCopy(Blocks.TERRACOTTA))
            .build();

    public static final ColoredBlockPreset CONCRETE = create()
            .properties(() -> BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_CONCRETE))
            .build();

    public static final ColoredBlockPreset CONCRETE_POWDER = create()
            .properties(() -> BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_CONCRETE_POWDER))
            .build();

    public static final ColoredBlockPreset CANDLE = create()
            .function(CandleBlock::new)
            .properties(() -> BlockBehaviour.Properties.ofFullCopy(Blocks.CANDLE))
            .build();

    public static ColoredBlockSet.PresetBuilder create() {
        return createFrom(DEFAULT);
    }

    public static ColoredBlockSet.PresetBuilder createFrom(ColoredBlockPreset preset) {
        return new ColoredBlockSet.PresetBuilder(preset.settings.copy());
    }
}