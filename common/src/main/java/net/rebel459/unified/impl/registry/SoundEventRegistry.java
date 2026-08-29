package net.rebel459.unified.impl.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.core.RegistryResourceListener;
import net.rebel459.unified.api.core.UnifiedRegistries;

import java.util.Optional;

public class SoundEventRegistry extends RegistryResourceListener<SoundEventRegistry.Definition> {
    public static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("fixed_range").forGetter(Definition::fixedRange)
    ).apply(instance, Definition::new));

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Unified.MOD_ID, "sound_events");

    public SoundEventRegistry() {
        super(ID, CODEC);
    }

    @Override
    protected void register(Identifier id, SoundEventRegistry.Definition definition) {
        if (definition.fixedRange.isPresent()) UnifiedRegistries.SoundEvents.create(id.getNamespace()).register(id.getPath(), definition.fixedRange.get());
        else UnifiedRegistries.SoundEvents.create(id.getNamespace()).register(id.getPath());
    }

    public record Definition(Optional<Float> fixedRange) {}
}
