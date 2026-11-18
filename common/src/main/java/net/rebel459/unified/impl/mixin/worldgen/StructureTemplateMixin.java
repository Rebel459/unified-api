package net.rebel459.unified.impl.mixin.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.rebel459.unified.impl.util.MarkedTemplate;
import net.rebel459.unified.impl.util.MarkedBlocks;
import net.rebel459.unified.impl.util.MarkedProcessor;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.BitSet;
import java.util.List;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin implements MarkedTemplate {

    @Shadow
    private Vec3i size;

    @Unique
    private long[] unified$marked = MarkedBlocks.EMPTY;

    @Unique
    private volatile @Nullable BitSet unified$markedBits;

    @Override
    public @Nullable BitSet getMarked() {
        return this.unified$markedBits;
    }

    @Unique
    private void unified$setMarked(long[] marked) {
        BitSet bits = null;
        int sizeX = this.size.getX();
        int sizeY = this.size.getY();
        int sizeZ = this.size.getZ();
        for (long packed : marked) {
            int x = BlockPos.getX(packed);
            int y = BlockPos.getY(packed);
            int z = BlockPos.getZ(packed);
            if (x < 0 || y < 0 || z < 0 || x >= sizeX || y >= sizeY || z >= sizeZ) continue;
            if (bits == null) bits = new BitSet(sizeX * sizeY * sizeZ);
            bits.set(MarkedProcessor.index(x, y, z, sizeX, sizeZ));
        }
        this.unified$marked = marked;
        this.unified$markedBits = bits;
    }

    @Inject(method = "fillFromWorld", at = @At("TAIL"))
    private void unified$fillMarked(Level level, BlockPos position, Vec3i size, boolean includeEntities, List<Block> ignoreBlocks, CallbackInfo ci) {
        if (size.getX() < 1 || size.getY() < 1 || size.getZ() < 1) return;
        this.unified$setMarked(MarkedBlocks.collect(level, position, size));
    }

    @Inject(method = "save", at = @At("RETURN"))
    private void unified$saveMarked(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        if (this.unified$marked.length > 0) cir.getReturnValue().putLongArray(MarkedBlocks.TEMPLATE_KEY, this.unified$marked);
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void unified$loadMarked(HolderGetter<Block> blockLookup, CompoundTag tag, CallbackInfo ci) {
        this.unified$setMarked(tag.getLongArray(MarkedBlocks.TEMPLATE_KEY).orElse(MarkedBlocks.EMPTY));
    }
}
