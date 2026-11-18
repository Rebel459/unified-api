package net.rebel459.unified.impl.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jspecify.annotations.Nullable;

import java.util.BitSet;
import java.util.List;

public final class MarkedProcessor extends StructureProcessor {

    private final BitSet marked;
    private final int sizeX;
    private final int sizeZ;
    private final List<Holder<StructureProcessorList>> processors;
    private final StructureProcessorType<MarkedProcessor> type = () -> MapCodec.unit(this);

    private MarkedProcessor(BitSet marked, Vec3i size, List<Holder<StructureProcessorList>> processors) {
        this.marked = marked;
        this.sizeX = size.getX();
        this.sizeZ = size.getZ();
        this.processors = processors;
    }

    public static @Nullable MarkedProcessor create(StructureTemplate template, List<Holder<StructureProcessorList>> processors) {
        BitSet marked = ((MarkedTemplate) template).getMarked();
        return marked == null ? null : new MarkedProcessor(marked, template.getSize(), processors);
    }

    public static int index(int x, int y, int z, int sizeX, int sizeZ) {
        return (y * sizeZ + z) * sizeX + x;
    }

    @Override
    public StructureTemplate.@Nullable StructureBlockInfo processBlock(LevelReader level, BlockPos targetPosition, BlockPos referencePos, StructureTemplate.StructureBlockInfo originalBlockInfo, StructureTemplate.StructureBlockInfo processedBlockInfo, StructurePlaceSettings settings) {
        BlockPos templateRelativePos = originalBlockInfo.pos();
        if (!this.marked.get(index(templateRelativePos.getX(), templateRelativePos.getY(), templateRelativePos.getZ(), this.sizeX, this.sizeZ))) {
            return processedBlockInfo;
        }
        for (Holder<StructureProcessorList> list : this.processors) {
            for (StructureProcessor processor : list.value().list()) {
                processedBlockInfo = processor.processBlock(level, targetPosition, referencePos, originalBlockInfo, processedBlockInfo, settings);
                if (processedBlockInfo == null) return null;
            }
        }
        return processedBlockInfo;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return this.type;
    }
}
