package net.rebel459.unified.api.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.core.UnifiedInstance;

import java.util.List;
import java.util.function.Function;

public class UnifiedRequirementTypes {

    private static ExtensibleCodec.Simple<Boolean> simple(String path, boolean predicate) {
        return ExtensibleCodecs.REQUIREMENT_TYPES.register(Identifier.fromNamespaceAndPath(Unified.MOD_ID, path), () -> predicate);
    }

    private static <T> ExtensibleCodec.Complex<Boolean, T> complex(String path, MapCodec<T> codec, Function<T, Boolean> predicate) {
        return ExtensibleCodecs.REQUIREMENT_TYPES.register(Identifier.fromNamespaceAndPath(Unified.MOD_ID, path), codec, predicate);
    }

    public static final ExtensibleCodec.Simple<Boolean> NEVER = simple("never", false);
    public static final ExtensibleCodec.Simple<Boolean> ALWAYS = simple("always", false);

    public static final ExtensibleCodec.Complex<Boolean, List<String>> MODS_LOADED = complex(
            "mods_loaded",
            Codec.list(ExtraCodecs.NON_EMPTY_STRING).fieldOf("mods"),
            definition -> {
                boolean loaded = true;
                for (String mod : definition) {
                    if (!UnifiedInstance.isModLoaded(mod)) {
                        loaded = false;
                        break;
                    }
                }
                return loaded;
            }
    );

    public static void init() {}
}

