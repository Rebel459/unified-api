package net.rebel459.unified.impl.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jspecify.annotations.Nullable;

public final class KeepOriginalProcessor implements StructureProcessor {

    public static final KeepOriginalProcessor INSTANCE = new KeepOriginalProcessor();
    private static final MapCodec<KeepOriginalProcessor> CODEC = MapCodec.unit(INSTANCE);

    private KeepOriginalProcessor() {}

    @Override
    public StructureTemplate.@Nullable StructureBlockInfo processBlock(LevelReader level, BlockPos targetPosition, BlockPos referencePos, BlockPos templateRelativePos, StructureTemplate.StructureBlockInfo processedBlockInfo, StructurePlaceSettings settings) {
        return processedBlockInfo.state().is(Blocks.STRUCTURE_VOID) ? null : processedBlockInfo;
    }

    @Override
    public MapCodec<KeepOriginalProcessor> codec() {
        return CODEC;
    }
}
