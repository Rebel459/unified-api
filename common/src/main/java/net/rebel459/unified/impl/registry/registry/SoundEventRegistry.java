package net.rebel459.unified.util.data.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.rebel459.unified.Unified;
import net.rebel459.unified.platform.UnifiedRegistries;
import net.rebel459.unified.util.codec.CodecUtils;
import net.rebel459.unified.util.registry.RegistryResourceListener;

import java.util.Optional;

public class SoundEventRegistry extends RegistryResourceListener<SoundEventRegistry.Definition> {
    public static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("fixed_range").forGetter(Definition::fixedRange)
    ).apply(instance, Definition::new));

    public SoundEventRegistry() {
        super(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "sound_events"), CODEC);
    }

    @Override
    protected void register(Identifier id, SoundEventRegistry.Definition definition) {
        if (definition.fixedRange.isPresent()) UnifiedRegistries.SoundEvents.create(id.getNamespace()).register(id.getPath(), definition.fixedRange.get());
        else UnifiedRegistries.SoundEvents.create(id.getNamespace()).register(id.getPath());
    }

    public record Definition(Optional<Float> fixedRange) {}
}
