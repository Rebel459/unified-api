package net.rebel459.unified.api.registry;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.codec.UnifiedCodecs;

import java.util.List;
import java.util.Map;

public class UnifiedDisplayItemCodecs {

    public static final ExtensibleCodec.Complex<CreativeModeTab.DisplayItemsGenerator, List<Either<ItemStack, ListEntry>>> DISPLAY_FROM_LIST = ExtensibleCodecs.DISPLAY_ITEMS.register(
            Unified.id("display_from_list"),
            Codec.either(UnifiedCodecs.ITEM_OR_STACK, ListEntry.CODEC).listOf().fieldOf("items"),
            definition -> ((_, output) -> {
                for (Either<ItemStack, ListEntry> entry : definition) {
                    if (entry.left().isPresent()) output.accept(entry.left().get());
                    if (entry.right().isPresent()) output.accept(entry.right().get().item(), entry.right().get().visibility());
                }
            })
    );

    public record ListEntry(ItemStack item, CreativeModeTab.TabVisibility visibility) {
        public static final Codec<ListEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UnifiedCodecs.ITEM_OR_STACK.fieldOf("item").forGetter(ListEntry::item),
                UnifiedCodecs.named(CreativeModeTab.TabVisibility.class).optionalFieldOf("visibility", CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS).forGetter(ListEntry::visibility)
        ).apply(instance, ListEntry::new));
    }

    public static final ExtensibleCodec.Complex<CreativeModeTab.DisplayItemsGenerator, ModEntry> DISPLAY_FROM_MOD = ExtensibleCodecs.DISPLAY_ITEMS.register(
            Unified.id("display_from_mod"),
            ModEntry.CODEC,
            definition -> ((_, output) -> {
                for (Map.Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
                    if (entry.getKey().identifier().getNamespace().equals(definition.mod)) {
                        output.accept(entry.getValue(), definition.visibility);
                    }
                }
            })
    );

    public record ModEntry(String mod, CreativeModeTab.TabVisibility visibility) {
        public static final MapCodec<ModEntry> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ExtraCodecs.NON_EMPTY_STRING.fieldOf("mod").forGetter(ModEntry::mod),
                UnifiedCodecs.named(CreativeModeTab.TabVisibility.class).optionalFieldOf("visibility", CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS).forGetter(ModEntry::visibility)
        ).apply(instance, ModEntry::new));
    }

    public static void init() {}
}
