package net.rebel459.unified.api.registry;

import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecBase;
import net.rebel459.unified.api.codec.ExtensibleCodecs;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public class UnifiedItemPredicateCodecs {
    public static final ExtensibleCodec.Complex<Predicate<ItemStack>, HolderSet<Item>> ITEMS = ExtensibleCodecs.ITEM_PREDICATES.register(
            Unified.id("items"),
            RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items"),
            items -> stack -> items.contains(stack.typeHolder())
    );

    public static final ExtensibleCodec.Complex<Predicate<ItemStack>, Map<DataComponentType<?>, Object>> COMPONENTS = ExtensibleCodecs.ITEM_PREDICATES.register(
            Unified.id("components"),
            DataComponentType.VALUE_MAP_CODEC.fieldOf("components"),
            components -> stack -> components.entrySet().stream()
                    .allMatch(entry -> Objects.equals(stack.get(entry.getKey()), entry.getValue()))
    );

    public static final ExtensibleCodecBase.Simple<Predicate<ItemStack>> IS_AXE = ExtensibleCodecs.ITEM_PREDICATES.register(
            Unified.id("is_axe"),
            () -> stack -> stack.getItem() instanceof AxeItem
    );

    public static void init() {}
}
