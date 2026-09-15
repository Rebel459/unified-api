package net.rebel459.unified.impl.data.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.core.StagedRegistry;
import net.rebel459.unified.api.core.RegistryResourceListener;
import net.rebel459.unified.api.core.Supplied;
import net.rebel459.unified.api.core.UnifiedRegistries;

import java.util.Optional;
import java.util.function.Supplier;

public class SoundEventRegistry extends RegistryResourceListener<SoundEventRegistry.Definition> {
    public static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("fixed_range").forGetter(Definition::fixedRange)
    ).apply(instance, Definition::new));

    public static final Identifier ID = Unified.id("sound_events");

    public SoundEventRegistry() {
        super(ID, CODEC, Registries.SOUND_EVENT);
    }

    @Override
    protected void register(Identifier id, DeferredDeclaration<Definition> declaration) {
        StagedRegistry.register(Registries.SOUND_EVENT, id, () -> registerDefinition(id, declaration));
    }

    public static Supplied<SoundEvent> registerDefinition(Identifier id, Supplier<Definition> definition) {
        UnifiedRegistries.SoundEvents sounds = UnifiedRegistries.SoundEvents.create(id.getNamespace());
        Optional<Float> fixedRange = definition.get().fixedRange();
        return fixedRange.isPresent() ? sounds.register(id.getPath(), fixedRange.orElseThrow()) : sounds.register(id.getPath());
    }

    public record Definition(Optional<Float> fixedRange) {}
}
