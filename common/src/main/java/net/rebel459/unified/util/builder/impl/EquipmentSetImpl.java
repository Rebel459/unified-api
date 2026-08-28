package net.rebel459.unified.util.builder.impl;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.rebel459.unified.platform.UnifiedEvents;
import net.rebel459.unified.platform.UnifiedHelpers;
import net.rebel459.unified.util.CreativeModeTabs;
import net.rebel459.unified.util.builder.EquipmentSet;
import net.rebel459.unified.util.registry.SuppliedItem;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class EquipmentSetImpl {

    public static Map<Identifier, List<Triple<EquipmentSet.Group, Supplier<? extends DataComponentType<?>>, ?>>> EQUIPMENT_COMPONENTS = Collections.synchronizedMap(new HashMap<>());
    public static Map<Identifier, List<Triple<EquipmentSet.Group, Supplier<? extends DataComponentType<?>>, DataComponentInitializers.SingleComponentInitializer<?>>>> EQUIPMENT_PROVIDED_COMPONENTS = Collections.synchronizedMap(new HashMap<>());
    public static Map<Identifier, List<Triple<EquipmentSet.Group, Supplier<? extends DataComponentType<?>>, ResourceKey<?>>>> EQUIPMENT_KEYED_COMPONENTS = Collections.synchronizedMap(new HashMap<>());
    public static Map<Identifier, List<Pair<EquipmentSet.Group, ItemAttributeModifiers.Entry>>> EQUIPMENT_ATTRIBUTES = Collections.synchronizedMap(new HashMap<>());

    public static Map<Identifier, EquipmentSet.PrecedingToolCreativeEntries> CREATIVE_TOOL_ENTRIES = Collections.synchronizedMap(new HashMap<>());
    public static Map<Identifier, EquipmentSet.PrecedingArmorCreativeEntries> CREATIVE_ARMOR_ENTRIES = Collections.synchronizedMap(new HashMap<>());

    public static final Set<ResourceKey<Item>> SKIPPED_ATTRIBUTE_ITEMS = Collections.synchronizedSet(new HashSet<>());

    public static void init(List<EquipmentSet> equipmentSets) {
        creativeEntries(equipmentSets);
        components(equipmentSets);
        attributes(equipmentSets);
    }

    private static void creativeEntries(List<EquipmentSet> equipmentSets) {
        for (EquipmentSet equipment : equipmentSets) {
            EquipmentSet.PrecedingArmorCreativeEntries precedingArmorItems = CREATIVE_ARMOR_ENTRIES.get(equipment.getId());
            if (precedingArmorItems != null) {
                if (equipment.hasArmor()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.COMBAT, precedingArmorItems.combatArmor().get(), equipment.getHelmet(), equipment.getChestplate(), equipment.getLeggings(), equipment.getBoots());
                if (equipment.hasAnimalArmor()) {
                    if (precedingArmorItems.combatHorseArmor() != null) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.COMBAT, precedingArmorItems.combatHorseArmor().get(), equipment.getHorseArmor());
                    if (precedingArmorItems.combatNautilusArmor() != null) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.COMBAT, precedingArmorItems.combatNautilusArmor().get(), equipment.getNautilusArmor());
                }
            }
            EquipmentSet.PrecedingToolCreativeEntries precedingToolItems = CREATIVE_TOOL_ENTRIES.get(equipment.getId());
            if (precedingToolItems != null && equipment.hasTools()) {
                UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.TOOLS_AND_UTILITIES, precedingToolItems.utilities().get(), equipment.getShovel(), equipment.getPickaxe(), equipment.getAxe(), equipment.getHoe());
                UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.COMBAT, precedingToolItems.combatSword().get(), equipment.getSword());
                UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.COMBAT, precedingToolItems.combatSpear().get(), equipment.getSpear());
                UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabs.COMBAT, precedingToolItems.combatAxe().get(), equipment.getAxe());
            }
        }
    }

    private static <T, Y> void components(List<EquipmentSet> equipmentSets) {
        for (EquipmentSet equipment : equipmentSets) {
            var components = EQUIPMENT_COMPONENTS.get(equipment.getId());
            if (components != null) for (Triple<EquipmentSet.Group, Supplier<? extends DataComponentType<?>>, ?> entry : components) {
                for (EquipmentSet.Target target : entry.getLeft().getTargets()) {
                    SuppliedItem targetItem = getItem(equipment, target);
                    if (targetItem != null) {
                        if (entry.getMiddle() == DataComponents.ATTRIBUTE_MODIFIERS) SKIPPED_ATTRIBUTE_ITEMS.add(targetItem.key());
                        UnifiedHelpers.DATA_COMPONENTS.add(targetItem, (DataComponentType<T>) entry.getMiddle().get(), (T) entry.getRight());
                    }
                }
            }
            var providedComponents = EQUIPMENT_PROVIDED_COMPONENTS.get(equipment.getId());
            if (providedComponents != null) for (Triple<EquipmentSet.Group, Supplier<? extends DataComponentType<?>>, DataComponentInitializers.SingleComponentInitializer<?>> entry : providedComponents) {
                for (EquipmentSet.Target target : entry.getLeft().getTargets()) {
                    SuppliedItem targetItem = getItem(equipment, target);
                    if (targetItem != null) UnifiedHelpers.DATA_COMPONENTS.addWithProvider(targetItem, (DataComponentType<T>) entry.getMiddle().get(), (DataComponentInitializers.SingleComponentInitializer<T>) entry.getRight());
                }
            }
            var keyedComponents = EQUIPMENT_KEYED_COMPONENTS.get(equipment.getId());
            if (keyedComponents != null) for (Triple<EquipmentSet.Group, Supplier<? extends DataComponentType<?>>, ResourceKey<?>> entry : keyedComponents) {
                for (EquipmentSet.Target target : entry.getLeft().getTargets()) {
                    SuppliedItem targetItem = getItem(equipment, target);
                    if (targetItem != null) UnifiedHelpers.DATA_COMPONENTS.addWithKey(targetItem, (DataComponentType<Holder<Y>>) entry.getMiddle().get(), (ResourceKey<Y>) entry.getRight());
                }
            }
        }
    }

    private static void attributes(List<EquipmentSet> equipmentSets) {
        for (EquipmentSet equipment : equipmentSets) {
            var attributes = EQUIPMENT_ATTRIBUTES.get(equipment.getId());
            HashMap<SuppliedItem, Pair<ItemAttributeModifiers.Builder, Set<Holder<Attribute>>>> attributeMap = new HashMap<>();
            if (attributes != null) for (Pair<EquipmentSet.Group, ItemAttributeModifiers.Entry> entry : attributes) {
                for (EquipmentSet.Target target : entry.getFirst().getTargets()) {
                    SuppliedItem targetItem = getItem(equipment, target);
                    if (targetItem != null) {
                        Pair<ItemAttributeModifiers.Builder, Set<Holder<Attribute>>> pair = attributeMap.get(targetItem);
                        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                        Set<Holder<Attribute>> addedAttributes = new HashSet<>();
                        if (pair != null) {
                            builder = pair.getFirst();
                            addedAttributes = pair.getSecond();
                        }
                        ItemAttributeModifiers.Entry modifier = entry.getSecond();
                        builder.add(modifier.attribute(), modifier.modifier(), modifier.slot(), modifier.display());
                        addedAttributes.add(modifier.attribute());
                        attributeMap.put(targetItem, Pair.of(builder, addedAttributes));
                    }
                }
            }
            for (SuppliedItem item : equipment.getRegisteredItems()) {
                if (SKIPPED_ATTRIBUTE_ITEMS.contains(item)) continue;
                UnifiedEvents.DefaultDataComponents.modifyWithFilter(
                        predicateItem -> item.get() == predicateItem,
                        (eventItem, eventBuilder, eventProvider) -> {
                            Pair<ItemAttributeModifiers.Builder, Set<Holder<Attribute>>> pair = attributeMap.get(item);
                            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                            Set<Holder<Attribute>> addedAttributes = new HashSet<>();
                            if (pair != null) {
                                builder = pair.getFirst();
                                addedAttributes = pair.getSecond();
                            }
                            var originalAttributes = item.defaultItemStack().get(DataComponents.ATTRIBUTE_MODIFIERS);
                            if (originalAttributes != null) for (ItemAttributeModifiers.Entry entry : originalAttributes.modifiers()) {
                                if (!addedAttributes.contains(entry.attribute())) builder.add(entry.attribute(), entry.modifier(), entry.slot(), entry.display());
                            }
                            eventBuilder.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
                        });
            }
        }
    }

    private static @Nullable SuppliedItem getItem(EquipmentSet equipment, EquipmentSet.Target target) {
        return switch (target) {
            case SWORD -> equipment.getSword();
            case SPEAR -> equipment.getSpear();
            case AXE -> equipment.getAxe();
            case PICKAXE -> equipment.getPickaxe();
            case SHOVEL -> equipment.getShovel();
            case HOE -> equipment.getHoe();
            case HELMET -> equipment.getHelmet();
            case CHESTPLATE -> equipment.getChestplate();
            case LEGGINGS -> equipment.getLeggings();
            case BOOTS -> equipment.getBoots();
            case HORSE_ARMOR -> equipment.getHorseArmor();
            case NAUTILUS_ARMOR -> equipment.getNautilusArmor();
        };
    }
}
