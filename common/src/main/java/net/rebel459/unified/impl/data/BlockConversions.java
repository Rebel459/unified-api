package net.rebel459.unified.impl.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.codec.UnifiedCodecs;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.core.UnifiedHelpers;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BlockConversions extends SimpleJsonResourceReloadListener<List<BlockConversions.Definition>> {

    public static final Identifier ID = Unified.id("block_conversions");

    public BlockConversions() {
        super(UnifiedCodecs.loadRequirements(BlockConversions.Definition.LIST_CODEC, List::of), FileToIdConverter.json(Unified.MOD_ID + "/" + ID.getPath()));
    }

    @Override
    protected void apply(@NonNull Map<Identifier, List<BlockConversions.Definition>> map, @NonNull ResourceManager resourceManager, @NonNull ProfilerFiller profilerFiller) {
        for (Map.Entry<Identifier, List<BlockConversions.Definition>> entry : map.entrySet()) {
            for (BlockConversions.Definition definition : entry.getValue()) {
                UnifiedHelpers.BLOCK_CONVERSIONS.add(definition.predicate.get(), definition.original, definition.converted, context -> {
                    definition.useContext.forEach(useContext -> useContext.get().accept(context));
                });
            }
        }
    }

    public record Definition(ExtensibleCodec.Entry<Predicate<ItemStack>> predicate, Block original, Block converted, List<ExtensibleCodec.Entry<Consumer<UseOnContext>>> useContext) {
        public static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExtensibleCodecs.ITEM_PREDICATE_TYPES.codec().fieldOf("predicate").forGetter(Definition::predicate),
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("original_block").forGetter(Definition::original),
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("converted_block").forGetter(Definition::converted),
                ExtensibleCodecs.USE_CONTEXT_TYPES.codec().listOf().optionalFieldOf("use_context", List.of()).forGetter(Definition::useContext)
        ).apply(instance, Definition::new));

        public static Codec<List<Definition>> LIST_CODEC = Codec.list(CODEC).fieldOf("entries").codec();
    }
}
