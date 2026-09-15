package net.rebel459.unified.api.data.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.rebel459.unified.api.codec.CodecGenerator;
import net.rebel459.unified.api.core.Supplied;
import net.rebel459.unified.impl.core.DataProviders;
import net.rebel459.unified.impl.data.registry.SoundEventRegistry;

import java.util.Optional;
import java.util.function.Supplier;

public class SoundEventGenerator {

    private final String modId;
    private final String namespace;
    private final DataProviders.GenerationSettings settings;

    public SoundEventGenerator(String modId, String namespace, DataProviders.GenerationSettings settings) {
        this.modId = modId;
        this.namespace = namespace;
        this.settings = settings;
    }

    public Supplied<SoundEvent> register(String path) {
        return register(path, Optional.empty());
    }

    public Supplied<SoundEvent> register(String path, float fixedRange) {
        return register(path, Optional.of(fixedRange));
    }

    private Supplied<SoundEvent> register(String path, Optional<Float> fixedRange) {
        Identifier id = Identifier.fromNamespaceAndPath(namespace, path);
        Supplier<SoundEventRegistry.Definition> definition = definition(fixedRange);
        Supplied<SoundEvent> sound = SoundEventRegistry.registerDefinition(id, definition);
        generate(id, definition);
        return sound;
    }

    private Supplier<SoundEventRegistry.Definition> definition(Optional<Float> fixedRange) {
        return () -> new SoundEventRegistry.Definition(fixedRange);
    }

    private void generate(Identifier id, Supplier<SoundEventRegistry.Definition> definition) {
        CodecGenerator.registry(modId, id, "sound_events", settings.metadata().priority(), settings.metadata().requirement(),
                SoundEventRegistry.CODEC, definition);
    }
}
