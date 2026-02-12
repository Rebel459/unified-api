package net.rebel459.unified.util;

import java.util.function.Supplier;

public final class UnifiedRenderStateDataKey<T> {
    private final Supplier<String> name;

    private UnifiedRenderStateDataKey(Supplier<String> debugName) {
        this.name = debugName;
    }

    public static <T> UnifiedRenderStateDataKey<T> create(Supplier<String> debugName) {
        return new UnifiedRenderStateDataKey<T>(debugName);
    }

    public static <T> UnifiedRenderStateDataKey<T> create() {
        return new UnifiedRenderStateDataKey<T>(() -> "unnamed");
    }

    public String toString() {
        return "RenderStateDataKey(" + (String)this.name.get() + ")";
    }
}