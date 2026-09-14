package net.rebel459.unified.impl.data.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.codec.UnifiedCodecs;
import net.rebel459.unified.api.core.DataRegistry;
import net.rebel459.unified.api.core.RegistryResourceListener;
import net.rebel459.unified.api.core.Supplied;
import net.rebel459.unified.api.core.UnifiedRegistries;
import net.rebel459.unified.api.registry.UnifiedCreativeModeTabs;
import net.rebel459.unified.api.registry.UnifiedDisplayItemCodecs;
import net.rebel459.unified.impl.platform.PlatformHandler;

import java.util.List;
import java.util.function.Supplier;

public class CreativeTabRegistry extends RegistryResourceListener<CreativeTabRegistry.Definition> {

    private static final Codec<Supplier<ItemStack>> SUPPLIED_STACK_CODEC = ItemStack.CODEC.xmap(stack -> stack::copy, Supplier::get);

    public static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UnifiedCodecs.named(CreativeModeTab.Row.class).optionalFieldOf("row", CreativeModeTab.Row.TOP).forGetter(Definition::row),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("column", 0).forGetter(Definition::column),
            ComponentSerialization.CODEC.optionalFieldOf("title", Component.empty()).forGetter(Definition::title),
            UnifiedCodecs.ITEM_OR_STACK.xmap(stack -> (Supplier<ItemStack>) stack::copy, Supplier::get)
                    .optionalFieldOf("icon", () -> ItemStack.EMPTY).forGetter(Definition::icon),
            ExtensibleCodecs.DISPLAY_ITEMS.codec().listOf().optionalFieldOf("display_items", List.of()).forGetter(Definition::displayItems),
            Codec.BOOL.optionalFieldOf("can_scroll", true).forGetter(Definition::canScroll),
            Codec.BOOL.optionalFieldOf("show_title", true).forGetter(Definition::showTitle),
            UnifiedCodecs.named(Alignment.class).optionalFieldOf("alignment", Alignment.LEFT).forGetter(Definition::alignment),
            Identifier.CODEC.optionalFieldOf("background_texture", CreativeModeTab.DEFAULT_BACKGROUND).forGetter(Definition::backgroundTexture)
    ).apply(instance, Definition::new));

    public static final Identifier ID = Unified.id("creative_tabs");

    public CreativeTabRegistry() {
        super(ID, CODEC, ItemRegistry.ID);
    }

    @Override
    protected void register(Identifier id, DeferredDeclaration<CreativeTabRegistry.Definition> declaration) {
        DataRegistry.register(Registries.CREATIVE_MODE_TAB, id, () -> {
            Definition definition = declaration.get();
            Supplier<CreativeModeTab> supplier = () -> {
                CreativeModeTab tab = PlatformHandler.INSTANCE.internal().createCreativeModeTab(definition.row, definition.column, definition.title, definition.icon, ((parameters, output) ->
                        definition.displayItems.forEach(entry -> entry.get().accept(parameters, output))
                ));
                tab.alignedRight = definition.alignment == Alignment.RIGHT;
                tab.showTitle = definition.showTitle;
                tab.canScroll = definition.canScroll;
                tab.backgroundTexture = definition.backgroundTexture;
                return tab;
            };
            return UnifiedRegistries.DeferredRegistry.create(id.getNamespace(), BuiltInRegistries.CREATIVE_MODE_TAB).register(id.getPath(), supplier);
        });
    }

    public record Definition(
            CreativeModeTab.Row row,
            int column,
            Component title,
            Supplier<ItemStack> icon,
            List<ExtensibleCodec.Entry<CreativeModeTab.DisplayItemsGenerator>> displayItems,
            boolean canScroll,
            boolean showTitle,
            Alignment alignment,
            Identifier backgroundTexture
    ) {}

    public enum Alignment {
        LEFT,
        RIGHT
    }
}
