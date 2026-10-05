package net.rebel459.unified.impl.data.helper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.codec.UnifiedCodecs;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.core.UnifiedHelpers;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BlockConversions {

    public static final Identifier ID = Unified.id("block_conversions");
    public static final ResourceKey<Registry<List<Definition>>> KEY =
            ResourceKey.createRegistryKey(ID);

    public static void init() {
        UnifiedHelpers.DATA_REGISTRIES.register(KEY,
                UnifiedCodecs.loadRequirements(Definition.LIST_CODEC, List::of));
    }

    public record Definition(ExtensibleCodec.Entry<Predicate<ItemStack>> predicate, Block original, Block converted, List<ExtensibleCodec.Entry<Consumer<UseOnContext>>> useContext) {
        public static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExtensibleCodecs.ITEM_PREDICATES.codec().fieldOf("predicate").forGetter(Definition::predicate),
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("original_block").forGetter(Definition::original),
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("converted_block").forGetter(Definition::converted),
                ExtensibleCodecs.USE_CONTEXT.codec().listOf().optionalFieldOf("use_context", List.of()).forGetter(Definition::useContext)
        ).apply(instance, Definition::new));

        public static Codec<List<Definition>> LIST_CODEC = Codec.list(CODEC).fieldOf("entries").codec();
    }
}
