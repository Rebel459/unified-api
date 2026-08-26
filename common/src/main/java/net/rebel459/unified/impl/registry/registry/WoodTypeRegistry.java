package net.rebel459.unified.util.data.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.rebel459.unified.Unified;
import net.rebel459.unified.registry.UnifiedBlockTypes;
import net.rebel459.unified.util.registry.RegistryResourceListener;

public class WoodTypeRegistry extends RegistryResourceListener<WoodTypeRegistry.Definition> {
    public static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("block_set_type").forGetter(Definition::blockSet),
            BlockRegistry.SoundType.CODEC.optionalFieldOf("sound_type", BlockRegistry.SoundType.create(SoundType.WOOD)).forGetter(Definition::soundType),
            BlockRegistry.SoundType.CODEC.optionalFieldOf("hanging_sign_sound_type", BlockRegistry.SoundType.create(SoundType.HANGING_SIGN)).forGetter(Definition::hangingSignSoundType),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("fence_gate_close", SoundEvents.FENCE_GATE_CLOSE).forGetter(Definition::fenceGateClose),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("fence_gate_open", SoundEvents.FENCE_GATE_OPEN).forGetter(Definition::fenceGateOpen)
    ).apply(instance, Definition::new));

    public WoodTypeRegistry() {
        super(Identifier.fromNamespaceAndPath(Unified.MOD_ID, "wood_types"), CODEC);
    }

    @Override
    protected void register(Identifier id, WoodTypeRegistry.Definition type) {
        WoodType.register(new WoodType(
                id.toString(),
                BlockSetType.TYPES.get(UnifiedBlockTypes.getSetName(id)),
                type.soundType.convert(),
                type.hangingSignSoundType.convert(),
                type.fenceGateClose,
                type.fenceGateOpen
        ));
    }

    public record Definition(Identifier blockSet, BlockRegistry.SoundType soundType, BlockRegistry.SoundType hangingSignSoundType, SoundEvent fenceGateClose, SoundEvent fenceGateOpen) {}
}
