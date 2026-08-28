package net.rebel459.unified.util.datagen.impl;

import net.rebel459.unified.util.datagen.ItemAsset;

import java.util.Objects;

public final class ItemAssetRequest<T> {
    private final ItemAsset<T> type;
    private final T value;

    private ItemAssetRequest(ItemAsset<T> type, T value) {
        this.type = Objects.requireNonNull(type);
        this.value = value;
    }

    public static ItemAssetRequest<Void> create(ItemAsset<Void> type) { return new ItemAssetRequest<>(type, null); }
    public static <T> ItemAssetRequest<T> create(ItemAsset<T> type, T value) { return new ItemAssetRequest<>(type, Objects.requireNonNull(value)); }
    public ItemAsset<T> type() { return type; }
    public T value() { return value; }
}
