package net.rebel459.unified.impl.codec;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.rebel459.unified.impl.core.DataProvider;

import java.util.function.BiFunction;

public record CodecRequest(String path, BiFunction<HolderLookup.Provider, DynamicOps<JsonElement>, JsonElement> encoder) {
    public static final DataProvider<CodecRequest> FILES = DataProvider.keyed(Identifier.fromNamespaceAndPath("unified", "json"), CodecRequest::path);
}
