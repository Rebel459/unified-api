package net.rebel459.unified.api.util;

import net.minecraft.world.level.block.Block;

public interface BlockLike {

    default Block asBlock() {
        throw new UnsupportedOperationException("BlockLike must be implemented by other classes");
    }
}
