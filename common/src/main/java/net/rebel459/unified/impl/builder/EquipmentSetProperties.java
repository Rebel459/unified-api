package net.rebel459.unified.impl.builder;

import net.minecraft.resources.Identifier;
import net.rebel459.unified.api.core.UnifiedHelpers;
import net.rebel459.unified.api.builder.EquipmentSet;
import net.rebel459.unified.api.registry.CreativeModeTabIds;

import java.util.*;

public class EquipmentSetProperties {

    public static Map<Identifier, EquipmentSet.PrecedingToolCreativeEntries> CREATIVE_TOOL_ENTRIES = Collections.synchronizedMap(new HashMap<>());
    public static Map<Identifier, EquipmentSet.PrecedingArmorCreativeEntries> CREATIVE_ARMOR_ENTRIES = Collections.synchronizedMap(new HashMap<>());

    public static void init(List<EquipmentSet> equipmentSets) {
        creativeEntries(equipmentSets);
    }

    private static void creativeEntries(List<EquipmentSet> equipmentSets) {
        for (EquipmentSet equipment : equipmentSets) {
            EquipmentSet.PrecedingArmorCreativeEntries precedingArmorItems = CREATIVE_ARMOR_ENTRIES.get(equipment.getId());
            if (precedingArmorItems != null) {
                if (equipment.hasArmor()) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabIds.COMBAT, precedingArmorItems.combatArmor().get(), equipment.getHelmet(), equipment.getChestplate(), equipment.getLeggings(), equipment.getBoots());
                if (equipment.hasAnimalArmor()) {
                    if (precedingArmorItems.combatHorseArmor() != null) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabIds.COMBAT, precedingArmorItems.combatHorseArmor().get(), equipment.getHorseArmor());
                    if (precedingArmorItems.combatNautilusArmor() != null) UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabIds.COMBAT, precedingArmorItems.combatNautilusArmor().get(), equipment.getNautilusArmor());
                }
            }
            EquipmentSet.PrecedingToolCreativeEntries precedingToolItems = CREATIVE_TOOL_ENTRIES.get(equipment.getId());
            if (precedingToolItems != null && equipment.hasTools()) {
                UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabIds.TOOLS_AND_UTILITIES, precedingToolItems.utilities().get(), equipment.getShovel(), equipment.getPickaxe(), equipment.getAxe(), equipment.getHoe());
                UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabIds.COMBAT, precedingToolItems.combatSword().get(), equipment.getSword());
                UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabIds.COMBAT, precedingToolItems.combatSpear().get(), equipment.getSpear());
                UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(CreativeModeTabIds.COMBAT, precedingToolItems.combatAxe().get(), equipment.getAxe());
            }
        }
    }
}
