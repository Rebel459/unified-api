package net.rebel459.unified.impl.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.core.UnifiedEvents;
import net.rebel459.unified.impl.worldgen.MarkedBlocks;

public final class ProcessorStick {

    private ProcessorStick() {}

    public static final Identifier ID = Unified.id("processor_stick");
    private static final CompoundTag TAG = createTag();

    private static CompoundTag createTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(ID.toString(), true);
        return tag;
    }

    public static void init() {
        UnifiedEvents.Items.onUseOn(ProcessorStick::useOn);
        UnifiedEvents.Entities.onEquipmentChange((entity, slot, oldStack, newStack) -> {
            if (!newStack.is(Items.STICK) || !(entity instanceof ServerPlayer player) || !player.canUseGameMasterBlocks() || !is(newStack)) return;
            MarkedBlocks.reveal(player);
        });
    }

    public static boolean is(ItemStack stack) {
        if (!stack.is(Items.STICK)) return false;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.matchedBy(TAG);
    }

    public static boolean isHolding(LivingEntity entity) {
        return is(entity.getMainHandItem()) || is(entity.getOffhandItem());
    }

    private static InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        if (!stack.is(Items.STICK)) return InteractionResult.PASS;
        Player user = context.getPlayer();
        if (user == null || !user.canUseGameMasterBlocks() || !is(stack)) return InteractionResult.PASS;
        InteractionHand otherHand = context.getHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        if (user.getItemInHand(otherHand).getItem() instanceof BlockItem) return InteractionResult.PASS;
        if (!(user instanceof ServerPlayer player) || !(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        MarkedBlocks.reveal(player);
        BlockPos pos = context.getClickedPos();
        boolean marked = MarkedBlocks.toggle(level, pos);
        player.sendOverlayMessage(Component.literal((marked ? "Marked " : "Unmarked ") + pos.toShortString()));
        if (MarkedBlocks.canSync(player)) return InteractionResult.SUCCESS;
        MarkedBlocks.showNearby(level, player);
        return InteractionResult.SUCCESS_SERVER;
    }
}
