package net.rebel459.unified.api.data.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.rebel459.unified.api.codec.CodecGenerator;
import net.rebel459.unified.impl.core.DataProviders;
import net.rebel459.unified.impl.data.registry.BlockRegistry;
import net.rebel459.unified.impl.data.registry.BlockSetTypeRegistry;

import java.util.function.Supplier;

public class BlockSetTypeGenerator {

    private final String modId;
    private final String namespace;
    private final DataProviders.GenerationSettings settings;

    public BlockSetTypeGenerator(String modId, String namespace, DataProviders.GenerationSettings settings) {
        this.modId = modId;
        this.namespace = namespace;
        this.settings = settings;
    }

    public Supplier<BlockSetType> register(String path, Supplier<BlockSetType> blockSetType) {
        Identifier id = Identifier.fromNamespaceAndPath(namespace, path);
        CodecGenerator.registry(modId, Identifier.fromNamespaceAndPath(namespace, path), "block_set_types", settings.metadata().priority(), settings.metadata().requirement(), BlockSetTypeRegistry.CODEC, () -> {
            BlockSetType type = blockSetType.get();
            if (!Identifier.parse(type.name()).equals(id)) throw new RuntimeException("Block Set Type name must match provided mod id & path");
            return new BlockSetTypeRegistry.Definition(
                    type.canOpenByHand(),
                    type.canOpenByWindCharge(),
                    type.canButtonBeActivatedByArrows(),
                    type.pressurePlateSensitivity(),
                    BlockRegistry.SoundType.create(type.soundType()),
                    type.doorClose(),
                    type.doorOpen(),
                    type.trapdoorClose(),
                    type.trapdoorOpen(),
                    type.pressurePlateClickOff(),
                    type.pressurePlateClickOn(),
                    type.buttonClickOff(),
                    type.buttonClickOn()
            );
        });
        return () -> {
            BlockSetType registered = BlockSetType.TYPES.get(id.toString());
            return registered != null ? registered : blockSetType.get();
        };
    }
}
