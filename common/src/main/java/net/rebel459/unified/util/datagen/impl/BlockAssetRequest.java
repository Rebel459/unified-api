package net.rebel459.unified.util.datagen.impl;

import net.rebel459.unified.util.datagen.BlockAsset;

import java.util.Objects;

public final class BlockAssetRequest<T> {
    private final BlockAsset<T> type;
    private final T value;

    private BlockAssetRequest(BlockAsset<T> type, T value) {
        this.type = Objects.requireNonNull(type);
        this.value = value;
    }

    public static BlockAssetRequest<Void> create(BlockAsset<Void> type) { return new BlockAssetRequest<>(type, null); }
    public static <T> BlockAssetRequest<T> create(BlockAsset<T> type, T value) { return new BlockAssetRequest<>(type, Objects.requireNonNull(value)); }
    public BlockAsset<T> type() { return type; }
    public T value() { return value; }
}
