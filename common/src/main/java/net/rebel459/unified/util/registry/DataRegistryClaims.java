package net.rebel459.unified.util.registry;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class DataRegistryClaims {
    private static final Map<Identifier, SuppliedBlock> BLOCKS = new ConcurrentHashMap<>();
    private static final Map<Identifier, SuppliedItem> ITEMS = new ConcurrentHashMap<>();
    private static final ThreadLocal<Identifier> ACTIVE_BLOCK = new ThreadLocal<>();
    private static final ThreadLocal<Identifier> ACTIVE_ITEM = new ThreadLocal<>();

    private DataRegistryClaims() {}

    public static @Nullable SuppliedBlock block(Identifier id) {
        return BLOCKS.get(id);
    }

    public static @Nullable SuppliedItem item(Identifier id) {
        return ITEMS.get(id);
    }

    @ApiStatus.Internal
    public static @Nullable SuppliedItem blockItem(SuppliedBlock block) {
        return block.item;
    }

    @ApiStatus.Internal
    public static void setBlockItem(SuppliedBlock block, @Nullable SuppliedItem item) {
        block.item = item;
    }

    public static boolean isRegisteringBlock(Identifier id) {
        return id.equals(ACTIVE_BLOCK.get());
    }

    public static boolean isRegisteringItem(Identifier id) {
        return id.equals(ACTIVE_ITEM.get());
    }

    public static SuppliedBlock registerBlock(Identifier id, Supplier<SuppliedBlock> registration) {
        if (ACTIVE_BLOCK.get() != null) throw new IllegalStateException("Nested JSON block registration: " + id);
        ACTIVE_BLOCK.set(id);
        try {
            SuppliedBlock block = registration.get();
            claimBlock(id, block);
            return block;
        } finally {
            ACTIVE_BLOCK.remove();
        }
    }

    public static SuppliedItem registerItem(Identifier id, Supplier<SuppliedItem> registration) {
        if (ACTIVE_ITEM.get() != null) throw new IllegalStateException("Nested JSON item registration: " + id);
        ACTIVE_ITEM.set(id);
        try {
            SuppliedItem item = registration.get();
            claimItem(id, item);
            return item;
        } finally {
            ACTIVE_ITEM.remove();
        }
    }

    public static void claimBlock(Identifier id, SuppliedBlock block) {
        SuppliedBlock previous = BLOCKS.putIfAbsent(id, block);
        if (previous != null && previous != block) {
            throw new IllegalStateException("JSON block identifier was claimed twice: " + id);
        }
        SuppliedItem item = block.item;
        if (item != null) claimItem(id, item);
    }

    public static void claimItem(Identifier id, SuppliedItem item) {
        SuppliedItem previous = ITEMS.putIfAbsent(id, item);
        if (previous != null && previous != item) {
            throw new IllegalStateException("JSON item identifier was claimed twice: " + id);
        }
    }
}
