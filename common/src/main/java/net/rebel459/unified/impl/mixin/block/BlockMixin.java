package net.rebel459.unified.impl.mixin.block;

import net.minecraft.world.level.block.Block;
import net.rebel459.unified.api.util.BlockLike;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Block.class)
public class BlockMixin implements BlockLike {

    @Override
    public Block asBlock() {
        return Block.class.cast(this);
    }
}