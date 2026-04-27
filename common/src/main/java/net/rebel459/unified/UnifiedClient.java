package net.rebel459.unified;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.rebel459.unified.client.util.helper.LegacyBabyArmorImpl;
import net.rebel459.unified.platform.client.UnifiedClientEvents;

public class UnifiedClient {

    public static void init() {
        LegacyBabyArmorImpl.init();
        UnifiedClientEvents.ItemTooltips.afterBaseAttributeAdded((builder, stack, itemModifiers, player, attribute, displayValue) -> {
            if (!attribute.is(Attributes.ATTACK_SPEED) || stack == null || !stack.has(DataComponents.TOOL)) return;

            Tool tool = stack.get(DataComponents.TOOL);
            float miningSpeed = tool.rules().stream()
                    .filter(rule -> rule.correctForDrops().orElse(false))
                    .flatMap(rule -> rule.speed().stream())
                    .max(Float::compare)
                    .orElse(tool.defaultMiningSpeed());

            if (miningSpeed == 1F) return;

            builder.accept(
                    Component.literal(" ")
                            .append(Component.literal(ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(miningSpeed) + " "))
                            .append(Component.translatable("tooltip.item_tooltips.mining_speed"))
                            .withStyle(ChatFormatting.DARK_GREEN)
            );
        });
    }
}
