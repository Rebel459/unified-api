package net.rebel459.unified.api.builder;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ColoredBlockPreset {

    final ColoredBlockSet.Settings settings;

    ColoredBlockPreset(ColoredBlockSet.Settings settings) {
        this.settings = settings;
    }

    public static final ColoredBlockPreset DEFAULT = new ColoredBlockSet.PresetBuilder()
            .build();

    public static final ColoredBlockPreset WOOL = create()
            .properties(() -> BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.white()))
            .setFlammability(30, 60, 100)
            .build();

    public static final ColoredBlockPreset TERRACOTTA = create()
            .properties(() -> BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_TERRACOTTA.white()))
            .build();

    public static final ColoredBlockPreset CONCRETE = create()
            .properties(() -> BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE.white()))
            .build();

    public static final ColoredBlockPreset CONCRETE_POWDER = create()
            .properties(() -> BlockBehaviour.Properties.ofFullCopy(Blocks.CONCRETE_POWDER.white()))
            .build();

    public static final ColoredBlockPreset CANDLE = create()
            .function(CandleBlock::new)
            .properties(() -> BlockBehaviour.Properties.ofFullCopy(Blocks.DYED_CANDLE.white()))
            .build();

    public static ColoredBlockSet.PresetBuilder create() {
        return createFrom(DEFAULT);
    }

    public static ColoredBlockSet.PresetBuilder createFrom(ColoredBlockPreset preset) {
        return new ColoredBlockSet.PresetBuilder(preset.settings.copy());
    }
}