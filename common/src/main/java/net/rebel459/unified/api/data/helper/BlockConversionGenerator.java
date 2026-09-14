package net.rebel459.unified.api.data.helper;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockTransformers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.rebel459.unified.api.codec.CodecGenerator;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.registry.UnifiedItemPredicateCodecs;
import net.rebel459.unified.api.registry.UnifiedUseContextCodecs;
import net.rebel459.unified.impl.data.helper.BlockConversions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BlockConversionGenerator extends HelperGenerator {
    BlockConversionGenerator(String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
        super(modId, requirement);
    }

    public Builder create(String name, Predicate<ItemStack> predicate, Block original, Block converted) {
        return create(name, ExtensibleCodecs.ITEM_PREDICATES.register(Identifier.fromNamespaceAndPath(modId, "block_conversions/" + name), () -> predicate).create(), original, converted);
    }

    public Builder create(String name, ExtensibleCodec.Entry<Predicate<ItemStack>> predicate, Block original, Block converted) {
        return new Builder(name, predicate, original, converted, modId, requirement);
    }

    public void addStrippable(String name, Block log, Block strippedLog) {
        new Builder(name, UnifiedItemPredicateCodecs.COMPONENTS.create(() -> Map.of(DataComponents.BLOCK_TRANSFORMER, BlockTransformers.AXE)), log, strippedLog, modId, requirement).onUse(UnifiedUseContextCodecs.PLAY_SOUND.create(SoundEvents.AXE_STRIP::value));
    }

    public static final class Builder extends HelperGenerator.Builder {

        private final List<ExtensibleCodec.Entry<Consumer<UseOnContext>>> useOnContext = new ArrayList<>();

        Builder(String name, ExtensibleCodec.Entry<Predicate<ItemStack>> predicate, Block original, Block converted, String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
            super(name, modId, requirement);
            CodecGenerator.data(modId, Identifier.fromNamespaceAndPath(modId, "unified/block_conversions/" + name), requirement, (_, ops) ->
                    BlockConversions.Definition.CODEC.encodeStart(ops,
                            new BlockConversions.Definition(
                                    predicate, original, converted, List.copyOf(useOnContext)
                            )).getOrThrow()
            );
        }

        public Builder onUse(Consumer<UseOnContext> context) {
            return onUse(ExtensibleCodecs.USE_CONTEXT.register(Identifier.fromNamespaceAndPath(modId, "block_conversions/" + name), () -> context).create());
        }

        public Builder onUse(ExtensibleCodec.Entry<Consumer<UseOnContext>> context) {
            useOnContext.add(context);
            return this;
        }
    }
}
