package net.rebel459.unified.impl.worldgen;

import org.jspecify.annotations.Nullable;

import java.util.BitSet;

public interface MarkedTemplate {

    @Nullable BitSet getMarked();
}
