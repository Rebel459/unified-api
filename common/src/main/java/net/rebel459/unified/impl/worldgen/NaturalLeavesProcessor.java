package net.rebel459.unified.impl.worldgen;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jspecify.annotations.Nullable;

public final class NaturalLeavesProcessor extends StructureProcessor {

    private final StructureProcessorType<NaturalLeavesProcessor> type = () -> MapCodec.unit(this);
    private final Long2IntOpenHashMap leaves = new Long2IntOpenHashMap();
    private final LongArrayList sources = new LongArrayList();

    private static boolean isLeaf(BlockState state) {
        return state.getBlock() instanceof LeavesBlock && state.hasProperty(LeavesBlock.PERSISTENT) && state.hasProperty(LeavesBlock.DISTANCE);
    }

    @Override
    public StructureTemplate.@Nullable StructureBlockInfo processBlock(LevelReader level, BlockPos targetPosition, BlockPos referencePos, StructureTemplate.StructureBlockInfo originalBlockInfo, StructureTemplate.StructureBlockInfo processedBlockInfo, StructurePlaceSettings settings) {
        BlockState state = processedBlockInfo.state();
        BlockPos pos = processedBlockInfo.pos();
        if (!isLeaf(state)) {
            if (state.is(BlockTags.PREVENTS_NEARBY_LEAF_DECAY)) this.sources.add(pos.asLong());
            return processedBlockInfo;
        }
        this.leaves.put(pos.asLong(), state.getValue(LeavesBlock.DISTANCE).intValue());
        BlockState processed = state.setValue(LeavesBlock.PERSISTENT, false);
        return processed == state ? processedBlockInfo : new StructureTemplate.StructureBlockInfo(pos, processed, processedBlockInfo.nbt());
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return this.type;
    }

    public void finish(WorldGenLevel level) {
        if (this.leaves.isEmpty()) return;
        Long2IntOpenHashMap distances = new Long2IntOpenHashMap(this.leaves.size());
        distances.defaultReturnValue(LeavesBlock.DECAY_DISTANCE);
        LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
        for (int i = 0; i < this.sources.size(); i++) {
            this.spread(this.sources.getLong(i), 0, distances, queue);
        }
        while (!queue.isEmpty()) {
            long packed = queue.dequeueLong();
            this.spread(packed, distances.get(packed), distances, queue);
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (Long2IntMap.Entry entry : this.leaves.long2IntEntrySet()) {
            int distance = distances.get(entry.getLongKey());
            if (distance == entry.getIntValue()) continue;
            cursor.set(entry.getLongKey());
            BlockState state = level.getBlockState(cursor);
            if (isLeaf(state)) level.setBlock(cursor, state.setValue(LeavesBlock.DISTANCE, distance), 18);
        }
    }

    private void spread(long packed, int distance, Long2IntOpenHashMap distances, LongArrayFIFOQueue queue) {
        int next = distance + 1;
        if (next >= LeavesBlock.DECAY_DISTANCE) return;
        int x = BlockPos.getX(packed);
        int y = BlockPos.getY(packed);
        int z = BlockPos.getZ(packed);
        this.visit(BlockPos.asLong(x + 1, y, z), next, distances, queue);
        this.visit(BlockPos.asLong(x - 1, y, z), next, distances, queue);
        this.visit(BlockPos.asLong(x, y + 1, z), next, distances, queue);
        this.visit(BlockPos.asLong(x, y - 1, z), next, distances, queue);
        this.visit(BlockPos.asLong(x, y, z + 1), next, distances, queue);
        this.visit(BlockPos.asLong(x, y, z - 1), next, distances, queue);
    }

    private void visit(long packed, int distance, Long2IntOpenHashMap distances, LongArrayFIFOQueue queue) {
        if (!this.leaves.containsKey(packed) || distances.get(packed) <= distance) return;
        distances.put(packed, distance);
        queue.enqueue(packed);
    }
}
