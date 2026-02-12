package net.rebel459.unified.util.fabric;

import java.util.function.Supplier;

public final class RenderStateDataKey<T> {
    private final Supplier<String> name;

    private RenderStateDataKey(Supplier<String> debugName) {
        this.name = debugName;
    }

    public static <T> RenderStateDataKey<T> create(Supplier<String> debugName) {
        return new RenderStateDataKey<T>(debugName);
    }

    public static <T> RenderStateDataKey<T> create() {
        return new RenderStateDataKey<>(() -> "unnamed");
    }

    public String toString() {
        return "RenderStateDataKey(" + this.name.get() + ")";
    }
}