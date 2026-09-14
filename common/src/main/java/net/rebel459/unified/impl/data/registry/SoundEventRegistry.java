package net.rebel459.unified.impl.data.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.core.RegistryResourceListener;
import net.rebel459.unified.api.core.UnifiedRegistries;

import java.util.Optional;

public class SoundEventRegistry extends RegistryResourceListener<SoundEventRegistry.Definition> {
    public static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("fixed_range").forGetter(Definition::fixedRange)
    ).apply(instance, Definition::new));

    public static final Identifier ID = Unified.id("sound_events");

    public SoundEventRegistry() {
        super(ID, CODEC);
    }

    @Override
    protected void register(Identifier id, DeferredDeclaration<Definition> declaration) {
        UnifiedRegistries.DeferredRegistry.create(id.getNamespace(), BuiltInRegistries.SOUND_EVENT)
                .register(id.getPath(), () -> declaration.get().fixedRange()
                        .map(range -> SoundEvent.createFixedRangeEvent(id, range))
                        .orElseGet(() -> SoundEvent.createVariableRangeEvent(id)));
    }

    public record Definition(Optional<Float> fixedRange) {}
}
