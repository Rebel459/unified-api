package net.rebel459.unified.api.data.helper;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.rebel459.unified.api.codec.CodecGenerator;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.registry.UnifiedItemPredicateCodecs;
import net.rebel459.unified.api.registry.UnifiedUseContextCodecs;
import net.rebel459.unified.api.util.BlockLike;
import net.rebel459.unified.impl.data.helper.BlockConversions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class BlockConversionGenerator extends HelperGenerator {
    public BlockConversionGenerator(String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
        super(modId, requirement);
    }

    public Builder createAndRegister(String name, Function<HolderLookup.Provider, Predicate<ItemStack>> predicate, BlockLike original, BlockLike converted) {
        return create(name, provider -> ExtensibleCodecs.ITEM_PREDICATES.register(Identifier.fromNamespaceAndPath(modId, "block_conversion/" + name), () -> predicate.apply(provider)).create(), original, converted);
    }

    public Builder create(String name, Function<HolderLookup.Provider, ExtensibleCodec.Entry<Predicate<ItemStack>>> predicate, BlockLike original, BlockLike converted) {
        return new Builder(name, predicate, original, converted, modId, requirement);
    }

    public void addStrippable(String name, BlockLike log, BlockLike strippedLog) {
        create(name, _ -> UnifiedItemPredicateCodecs.IS_AXE.create(), log, strippedLog).onUse(UnifiedUseContextCodecs.PLAY_SOUND.create(() -> SoundEvents.AXE_STRIP));
    }

    public static final class Builder extends HelperGenerator.Builder {

        private final List<ExtensibleCodec.Entry<Consumer<UseOnContext>>> useOnContext = new ArrayList<>();

        Builder(String name, Function<HolderLookup.Provider, ExtensibleCodec.Entry<Predicate<ItemStack>>> predicate, BlockLike original, BlockLike converted, String modId, Optional<ExtensibleCodec.Entry<Boolean>> requirement) {
            super(name, modId, requirement);
            CodecGenerator.data(modId, Identifier.fromNamespaceAndPath(modId, "unified/block_conversions/" + name), requirement, (registries, ops) ->
                    BlockConversions.Definition.CODEC.encodeStart(ops,
                            new BlockConversions.Definition(
                                    predicate.apply(registries), original.asBlock(), converted.asBlock(), List.copyOf(useOnContext)
                            )).getOrThrow()
            );
        }

        public BlockConversionGenerator.Builder onUse(Consumer<UseOnContext> context) {
            return onUse(ExtensibleCodecs.USE_CONTEXT.register(Identifier.fromNamespaceAndPath(modId, "block_conversions/" + name), () -> context).create());
        }

        public BlockConversionGenerator.Builder onUse(ExtensibleCodec.Entry<Consumer<UseOnContext>> context) {
            useOnContext.add(context);
            return this;
        }
    }
}
