package net.rebel459.unified.util.builder;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;

public class EquipmentPreset {

    final EquipmentSet.Settings settings;

    EquipmentPreset(EquipmentSet.Settings settings) {
        this.settings = settings;
    }

    public static final EquipmentPreset DEFAULT = new EquipmentSet.PresetBuilder()
            .build();

    public static final EquipmentPreset WOOD = createFrom(ToolMaterial.WOOD)
            .setAxeSwingSpeed(0.8F)
            .setSpearProperties(0.65F, 0.7F, 0.75F, 5.0F, 14.0F, 10.0F, 5.1F, 15.0F, 4.6F)
            .build();

    public static final EquipmentPreset LEATHER = createFrom(ArmorMaterials.LEATHER)
            .build();

    public static final EquipmentPreset STONE = createFrom(ToolMaterial.STONE)
            .setAxeSwingSpeed(0.8F)
            .setSpearProperties(0.75F, 0.82F, 0.7F, 4.5F, 13.0F, 9.0F, 5.1F, 13.75F, 4.6F)
            .build();

    public static final EquipmentPreset COPPER = createFrom(ToolMaterial.COPPER, ArmorMaterials.COPPER)
            .setAxeSwingSpeed(0.8F)
            .setSpearProperties(0.85F, 0.82F, 0.65F, 4.0F, 12.0F, 8.25F, 5.1F, 12.5F, 4.6F)
            .build();

    public static final EquipmentPreset CHAINMAIL = createFrom(ArmorMaterials.CHAINMAIL)
            .build();

    public static final EquipmentPreset IRON = createFrom(ToolMaterial.IRON, ArmorMaterials.IRON)
            .setAxeSwingSpeed(0.9F)
            .setSpearProperties(0.95F, 0.95F, 0.6F, 2.5F, 11.0F, 6.75F, 5.1F, 11.25F, 4.6F)
            .build();

    public static final EquipmentPreset GOLD = createFrom(ToolMaterial.GOLD, ArmorMaterials.GOLD)
            .setSpearProperties(0.95F, 0.7F, 0.7F, 3.5F, 13.0F, 8.5F, 5.1F, 13.75F, 4.6F)
            .build();

    public static final EquipmentPreset DIAMOND = createFrom(ToolMaterial.DIAMOND, ArmorMaterials.DIAMOND)
            .setSpearProperties(1.05F, 1.075F, 0.5F, 3.0F, 10.0F, 6.5F, 5.1F, 10.0F, 4.6F)
            .build();

    public static final EquipmentPreset NETHERITE = createFrom(ToolMaterial.NETHERITE, ArmorMaterials.NETHERITE)
            .setComponentWithProvider(EquipmentSet.Group.ALL, DataComponents.DAMAGE_RESISTANT, provider -> new DamageResistant(provider.getOrThrow(DamageTypeTags.IS_FIRE)))
            .build();

    public static EquipmentSet.PresetBuilder create() {
        return createFrom(DEFAULT);
    }

    public static EquipmentSet.PresetBuilder createFrom(EquipmentPreset preset) {
        return new EquipmentSet.PresetBuilder(preset.settings.copy());
    }
    public static EquipmentSet.PresetBuilder createFrom(ToolMaterial material) {
        return create()
                .setDamageBonus(material.attackDamageBonus())
                .setMiningSpeed(material.speed())
                .setEnchantingPower(material.enchantmentValue())
                .setToolDurability(material.durability())
                .setIncorrectBlocksForDrops(material.incorrectBlocksForDrops())
                .setRepairMaterials(material.repairItems());
    }
    public static EquipmentSet.PresetBuilder createFrom(ArmorMaterial material) {
        return create()
                .setEnchantingPower(material.enchantmentValue())
                .setArmorDurabilityFactor(material.durability())
                .setArmorToughness(material.toughness())
                .setKnockbackResistance(material.knockbackResistance())
                .setArmorDefense(material.defense().get(ArmorType.HELMET), material.defense().get(ArmorType.CHESTPLATE), material.defense().get(ArmorType.LEGGINGS), material.defense().get(ArmorType.BOOTS), material.defense().get(ArmorType.BODY))
                .setRepairMaterials(material.repairIngredient())
                .setArmorEquipSound(material.equipSound());
    }
    public static EquipmentSet.PresetBuilder createFrom(ToolMaterial toolMaterial, ArmorMaterial armorMaterial) {
        return create()
                .setEnchantingPower(toolMaterial.enchantmentValue(), armorMaterial.enchantmentValue())
                .setDamageBonus(toolMaterial.attackDamageBonus())
                .setMiningSpeed(toolMaterial.speed())
                .setToolDurability(toolMaterial.durability())
                .setIncorrectBlocksForDrops(toolMaterial.incorrectBlocksForDrops())
                .setRepairMaterials(toolMaterial.repairItems())
                .setArmorDurabilityFactor(armorMaterial.durability())
                .setArmorToughness(armorMaterial.toughness())
                .setKnockbackResistance(armorMaterial.knockbackResistance())
                .setArmorDefense(armorMaterial.defense().get(ArmorType.HELMET), armorMaterial.defense().get(ArmorType.CHESTPLATE), armorMaterial.defense().get(ArmorType.LEGGINGS), armorMaterial.defense().get(ArmorType.BOOTS), armorMaterial.defense().get(ArmorType.BODY))
                .setArmorEquipSound(armorMaterial.equipSound());
    }
}
