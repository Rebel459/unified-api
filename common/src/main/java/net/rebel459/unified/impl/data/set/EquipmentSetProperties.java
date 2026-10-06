package net.rebel459.unified.impl.data.set;

import net.minecraft.resources.Identifier;
import net.rebel459.unified.api.data.set.EquipmentSet;
import net.rebel459.unified.api.data.helper.CreativeEntryGenerator;
import net.rebel459.unified.api.registry.CreativeModeTabIds;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipmentSetProperties {

    public static Map<Identifier, EquipmentSet.PrecedingToolCreativeEntries> CREATIVE_TOOL_ENTRIES = Collections.synchronizedMap(new HashMap<>());
    public static Map<Identifier, EquipmentSet.PrecedingArmorCreativeEntries> CREATIVE_ARMOR_ENTRIES = Collections.synchronizedMap(new HashMap<>());
    public static Map<Identifier, CreativeEntryGenerator> CREATIVE_ENTRY_GENERATORS = Collections.synchronizedMap(new HashMap<>());

    public static void init(List<EquipmentSet> equipmentSets) {
        creativeEntries(equipmentSets);
    }

    private static void creativeEntries(List<EquipmentSet> equipmentSets) {
        for (EquipmentSet equipment : equipmentSets) {
            CreativeEntryGenerator generator = CREATIVE_ENTRY_GENERATORS.get(equipment.getId());
            if (generator == null) continue;
            CreativeEntryGenerator.Builder builder = generator.create("equipment_set/" + equipment.getId().getPath());

            EquipmentSet.PrecedingArmorCreativeEntries precedingArmorItems = CREATIVE_ARMOR_ENTRIES.get(equipment.getId());
            if (precedingArmorItems != null) {
                if (equipment.hasArmor()) builder.insertAfter(CreativeModeTabIds.COMBAT, precedingArmorItems.combatArmor().get(), equipment.getHelmet(), equipment.getChestplate(), equipment.getLeggings(), equipment.getBoots());
                if (equipment.hasAnimalArmor()) {
                    if (precedingArmorItems.combatHorseArmor() != null) builder.insertAfter(CreativeModeTabIds.COMBAT, precedingArmorItems.combatHorseArmor().get(), equipment.getHorseArmor());
                    if (precedingArmorItems.combatNautilusArmor() != null) builder.insertAfter(CreativeModeTabIds.COMBAT, precedingArmorItems.combatNautilusArmor().get(), equipment.getNautilusArmor());
                }
            }
            EquipmentSet.PrecedingToolCreativeEntries precedingToolItems = CREATIVE_TOOL_ENTRIES.get(equipment.getId());
            if (precedingToolItems != null && equipment.hasTools()) {
                builder.insertAfter(CreativeModeTabIds.TOOLS_AND_UTILITIES, precedingToolItems.utilities().get(), equipment.getShovel(), equipment.getPickaxe(), equipment.getAxe(), equipment.getHoe());
                builder.insertAfter(CreativeModeTabIds.COMBAT, precedingToolItems.combatSword().get(), equipment.getSword());
                builder.insertAfter(CreativeModeTabIds.COMBAT, precedingToolItems.combatSpear().get(), equipment.getSpear());
                builder.insertAfter(CreativeModeTabIds.COMBAT, precedingToolItems.combatAxe().get(), equipment.getAxe());
            }
        }
    }
}
