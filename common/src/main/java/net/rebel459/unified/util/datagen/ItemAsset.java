package net.rebel459.unified.util.datagen;

import net.minecraft.resources.Identifier;

import java.util.Objects;

/** Identifies a loader-bound item asset generator with an argument of type {@code T}. */
public final class ItemAsset<T> {
    private final Identifier id;

    public ItemAsset(Identifier id) {
        this.id = id;
    }

    public Identifier id() {
        return id;
    }

    @Override public boolean equals(Object other) {
        return this == other || other instanceof ItemAsset<?> type && id.equals(type.id);
    }
    @Override public int hashCode() {
        return id.hashCode();
    }
    @Override public String toString() {
        return id.toString();
    }
}
