package net.rebel459.unified.util;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface BlockConversions {

    default void createStrippable(Block originalBlock, Block convertedBlock) {
        create(stack -> stack.getItem() instanceof AxeItem, originalBlock, convertedBlock, SoundEvents.AXE_STRIP);
    }

    default void create(Predicate<ItemStack> validItem, Block originalBlock, Block convertedBlock, SoundEvent sound) {
        create(validItem, originalBlock, convertedBlock, sound, 1F, 1F);
    }
    default void create(Predicate<ItemStack> validItem, Block originalBlock, Block convertedBlock, SoundEvent sound, float volume, float pitch) {
        BlockConversions.Impl.ITEM_INTERACTIONS.put(originalBlock, new BlockConversions.Impl.Record(validItem, convertedBlock, (context) -> {
            Player player = context.getPlayer();
            if (player == null) return;
            context.getLevel().playSound(player, context.getClickedPos(), sound, SoundSource.BLOCKS, volume, pitch);
            context.getItemInHand().hurtAndBreak(1, player, player.getEquipmentSlotForItem(context.getItemInHand()));
        }));
    }
    default void create(Predicate<ItemStack> validItem, Block originalBlock, Block convertedBlock, Consumer<UseOnContext> context) {
        BlockConversions.Impl.ITEM_INTERACTIONS.put(originalBlock, new BlockConversions.Impl.Record(validItem, convertedBlock, context));
    }

    class Impl {
        public static InteractionResult useOn(UseOnContext context) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            Player player = context.getPlayer();
            if (playerHasBlockingItemUseIntent(context)) {
                return InteractionResult.PASS;
            } else {
                ItemStack itemInHand = context.getItemInHand();
                Optional<BlockState> newBlock = evaluateNewBlockState(context, level.getBlockState(pos));
                if (newBlock.isEmpty()) {
                    return InteractionResult.PASS;
                } else {
                    if (player instanceof ServerPlayer) {
                        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) player, pos, itemInHand);
                    }

                    level.setBlock(pos, newBlock.get(), 11);
                    level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, newBlock.get()));

                    return InteractionResult.SUCCESS;
                }
            }
        }

        private static boolean playerHasBlockingItemUseIntent(UseOnContext context) {
            Player player = context.getPlayer();
            return context.getHand().equals(InteractionHand.MAIN_HAND) && player.getOffhandItem().has(DataComponents.BLOCKS_ATTACKS) && !player.isSecondaryUseActive();
        }

        private static Optional<BlockState> evaluateNewBlockState(UseOnContext context, BlockState oldState) {
            var interaction = ITEM_INTERACTIONS.get(oldState.getBlock());
            if (context.getPlayer() == null || interaction == null || !interaction.validItem.test(context.getItemInHand()))
                return Optional.empty();
            interaction.context.accept(context);
            return Optional.of(interaction.convertedBlock.withPropertiesOf(oldState));
        }

        record Record(Predicate<ItemStack> validItem, Block convertedBlock, Consumer<UseOnContext> context) {}

        static HashMap<Block, Record> ITEM_INTERACTIONS = new HashMap<>();
    }
}
