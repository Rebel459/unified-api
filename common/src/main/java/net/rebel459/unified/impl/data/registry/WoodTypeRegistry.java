package net.rebel459.unified.impl.data.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.codec.UnifiedCodecs;
import net.rebel459.unified.api.core.RegistryResourceListener;
import net.rebel459.unified.impl.platform.PlatformHandler;

public class WoodTypeRegistry extends RegistryResourceListener<WoodTypeRegistry.Definition> {
    public static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UnifiedCodecs.BLOCK_SET_TYPE.fieldOf("block_set_type").forGetter(Definition::blockSet),
            BlockRegistry.SoundType.CODEC.optionalFieldOf("sound_type", BlockRegistry.SoundType.create(SoundType.WOOD)).forGetter(Definition::soundType),
            BlockRegistry.SoundType.CODEC.optionalFieldOf("hanging_sign_sound_type", BlockRegistry.SoundType.create(SoundType.HANGING_SIGN)).forGetter(Definition::hangingSignSoundType),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("fence_gate_close", SoundEvents.FENCE_GATE_CLOSE).forGetter(Definition::fenceGateClose),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("fence_gate_open", SoundEvents.FENCE_GATE_OPEN).forGetter(Definition::fenceGateOpen)
    ).apply(instance, Definition::new));

    public static final Identifier ID = Unified.id("wood_types");

    public WoodTypeRegistry() {
        super(ID, CODEC, BlockSetTypeRegistry.ID);
    }

    @Override
    protected void register(Identifier id, DeferredDeclaration<WoodTypeRegistry.Definition> declaration) {
        PlatformHandler.INSTANCE.internal().afterRegistry(Registries.SOUND_EVENT, () -> actualRegister(id, declaration.get()));
    }

    private static void actualRegister(Identifier id, WoodTypeRegistry.Definition type) {
        WoodType.register(new WoodType(
                id.toString(),
                type.blockSet,
                type.soundType.convert(),
                type.hangingSignSoundType.convert(),
                type.fenceGateClose,
                type.fenceGateOpen
        ));
    }

    public record Definition(BlockSetType blockSet, BlockRegistry.SoundType soundType, BlockRegistry.SoundType hangingSignSoundType, SoundEvent fenceGateClose, SoundEvent fenceGateOpen) {}
}
