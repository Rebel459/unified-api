package net.rebel459.unified.api.codec;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.rebel459.unified.impl.codec.CodecRequest;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/** Used to generate codecs as JSON */
public final class CodecGenerator {
    private CodecGenerator() {}

    public static <T> void registry(String modId, Identifier id, String directory, int priority, Optional<ExtensibleCodec.Entry<Boolean>> requirement, Codec<T> codec, Supplier<T> value) {
        data(modId, Identifier.fromNamespaceAndPath(id.getNamespace(), "unified/registry/" + directory + "/" + id.getPath()), requirement, (_, ops) -> {
            JsonObject json = codec.encodeStart(ops, value.get()).getOrThrow().getAsJsonObject();
            if (priority != 0) json.addProperty("priority", priority);
            return json;
        });
    }

    public static <T> void data(String modId, Identifier file, Optional<ExtensibleCodec.Entry<Boolean>> requirement, Codec<T> codec, Supplier<T> value) {
        data(modId, file, requirement, (_, ops) -> codec.encodeStart(ops, value.get()).getOrThrow());
    }
    public static void data(String modId, Identifier file, Optional<ExtensibleCodec.Entry<Boolean>> requirement, BiFunction<HolderLookup.Provider, DynamicOps<JsonElement>, JsonElement> encoder) {
        write(modId, "data", file, requirement, encoder);
    }

    public static <T> void assets(String modId, Identifier file, Optional<ExtensibleCodec.Entry<Boolean>> requirement, Codec<T> codec, Supplier<T> value) {
        assets(modId, file, requirement, (_, ops) -> codec.encodeStart(ops, value.get()).getOrThrow());
    }
    public static void assets(String modId, Identifier file, Optional<ExtensibleCodec.Entry<Boolean>> requirement, BiFunction<HolderLookup.Provider, DynamicOps<JsonElement>, JsonElement> encoder) {
        write(modId, "assets", file, requirement, encoder);
    }

    private static void write(String modId, String root, Identifier file, Optional<ExtensibleCodec.Entry<Boolean>> requirement, BiFunction<HolderLookup.Provider, DynamicOps<JsonElement>, JsonElement> encoder) {
        for (String segment : file.getPath().split("/", -1)) {
            if (segment.isEmpty() || segment.equals(".") || segment.equals("..")) {
                throw new IllegalArgumentException("Invalid JSON file path " + file);
            }
        }
        CodecRequest.FILES.add(modId, new CodecRequest(
                root + "/" + file.getNamespace() + "/" + file.getPath() + ".json",
                (registries, ops) -> {
                    JsonElement encoded = encoder.apply(registries, ops);
                    if (requirement.isEmpty()) return encoded;
                    JsonObject json = encoded.getAsJsonObject();
                    json.add("load_requirements", ExtensibleCodecs.REQUIREMENT_TYPES.codec()
                            .encodeStart(ops, requirement.orElseThrow()).getOrThrow());
                    return json;
                }));
    }
}
