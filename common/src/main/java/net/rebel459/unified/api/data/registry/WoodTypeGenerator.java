package net.rebel459.unified.api.data.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.rebel459.unified.api.codec.CodecGenerator;
import net.rebel459.unified.impl.core.DataProviders;
import net.rebel459.unified.impl.data.registry.BlockRegistry;
import net.rebel459.unified.impl.data.registry.BlockSetTypeRegistry;
import net.rebel459.unified.impl.data.registry.WoodTypeRegistry;

import java.util.function.Supplier;

public class WoodTypeGenerator {

    private final String modId;
    private final String namespace;
    private final DataProviders.GenerationSettings settings;

    public WoodTypeGenerator(String modId, String namespace, DataProviders.GenerationSettings settings) {
        this.modId = modId;
        this.namespace = namespace;
        this.settings = settings;
    }

    public Supplier<WoodType> register(String path, Supplier<WoodType> woodType) {
        Identifier id = Identifier.fromNamespaceAndPath(namespace, path);
        Supplier<WoodTypeRegistry.Definition> definition = () -> {
            WoodType type = woodType.get();
            if (!Identifier.parse(type.name()).equals(id)) throw new RuntimeException("Wood Type name must match provided mod id & path");
            return new WoodTypeRegistry.Definition(
                    type.setType(),
                    BlockRegistry.SoundType.create(type.soundType()),
                    BlockRegistry.SoundType.create(type.hangingSignSoundType()),
                    type.fenceGateClose(),
                    type.fenceGateOpen()
            );
        };

        CodecGenerator.registry(modId, id, "wood_types", settings.metadata().priority(), settings.metadata().requirement(), WoodTypeRegistry.CODEC, definition);
        return () -> {
            WoodType registered = WoodType.TYPES.get(id.toString());
            return registered != null ? registered : woodType.get();
        };
    }
}
