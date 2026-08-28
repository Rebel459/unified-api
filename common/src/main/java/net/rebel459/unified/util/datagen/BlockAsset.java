package net.rebel459.unified.util.datagen;

import net.minecraft.resources.Identifier;

import java.util.Objects;

/** Identifies a loader-bound block asset generator with an argument of type {@code T}. */
public final class BlockAsset<T> {
    private final Identifier id;

    public BlockAsset(Identifier id) {
        this.id = id;
    }

    public Identifier id() { return id; }

    @Override public boolean equals(Object other) {
        return this == other || other instanceof BlockAsset<?> type && id.equals(type.id);
    }
    @Override public int hashCode() {
        return id.hashCode();
    }
    @Override public String toString() {
        return id.toString();
    }
}
