package net.rebel459.unified.impl.mixin.registry;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceManagerRegistryLoadTask;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.UnifiedCodecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(ResourceManagerRegistryLoadTask.class)
public abstract class ResourceManagerRegistryLoadTaskMixin {

    @Shadow @Final private ResourceManager resourceManager;

    @Inject(method = "lambda$load$0", at = @At("RETURN"), cancellable = true)
    private void addConditionalRecipes(FileToIdConverter lister, CallbackInfoReturnable<Map<Identifier, Resource>> cir) {
        String directory;
        if (lister.equals(FileToIdConverter.registry(Registries.RECIPE))) directory = "unified/recipes";
        else if (lister.equals(FileToIdConverter.registry(Registries.ADVANCEMENT))) directory = "unified/advancements";
        else return;

        Map<Identifier, Resource> resources = new LinkedHashMap<>(cir.getReturnValue());
        FileToIdConverter conditionalConverter = FileToIdConverter.json(directory);
        conditionalConverter.listMatchingResources(resourceManager).forEach((file, resource) -> {
            Identifier id = conditionalConverter.fileToId(file);
            Resource enabled = enabled(file, resource);
            if (enabled != null) resources.put(lister.idToFile(id), enabled);
        });
        cir.setReturnValue(resources);
    }

    private static Resource enabled(Identifier file, Resource resource) {
        try {
            JsonObject json;
            try (var reader = resource.openAsReader()) {
                json = JsonParser.parseReader(reader).getAsJsonObject();
            }
            boolean enabled = UnifiedCodecs.LOAD_REQUIREMENTS.codec().parse(JsonOps.INSTANCE, json)
                    .getOrThrow(error -> new IllegalArgumentException(file + " from " + resource.sourcePackId() + ": " + error))
                    .map(ExtensibleCodec.Entry::get).orElse(true);
            if (!enabled) return null;

            json.remove("load_requirement");
            byte[] contents = json.toString().getBytes(StandardCharsets.UTF_8);
            return new Resource(resource.source(), () -> new ByteArrayInputStream(contents), resource::metadata);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to read conditional registry resource " + file + " from " + resource.sourcePackId(), exception);
        }
    }
}
