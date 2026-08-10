package net.rebel459.unified.impl.helper;

import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.rebel459.unified.api.util.BlockLike;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BlockConversionsImpl {
    public static InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (context.getHand().equals(InteractionHand.MAIN_HAND) && player.getOffhandItem().has(DataComponents.BLOCKS_ATTACKS) && !player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        } else {
            ItemStack itemInHand = context.getItemInHand();
            Optional<BlockState> newBlock = evaluateNewBlockState(context, level.getBlockState(pos));
            if (newBlock.isEmpty()) {
                return InteractionResult.PASS;
            } else {
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, itemInHand);
                }

                level.setBlock(pos, newBlock.get(), 11);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, newBlock.get()));

                return InteractionResult.SUCCESS;
            }
        }
    }

    private static Optional<BlockState> evaluateNewBlockState(UseOnContext context, BlockState oldState) {
        List<Record> interactions = INTERACTIONS.get(oldState.getBlock());
        if (context.getPlayer() == null || interactions == null) return Optional.empty();

        Record interaction = null;
        for (int i = interactions.size() - 1; i >= 0; i--) {
            Record candidate = interactions.get(i);
            if (candidate.validItem.test(context.getItemInHand())) {
                interaction = candidate;
                break;
            }
        }

        if (interaction == null) return Optional.empty();

        interaction.context.accept(context);
        return Optional.of(interaction.convertedBlock.withPropertiesOf(oldState));
    }

    public record Record(Predicate<ItemStack> validItem, Block convertedBlock, Consumer<UseOnContext> context) {}

    public static HashMap<Block, List<Record>> INTERACTIONS = new HashMap<>();

    public interface Oxidizables {
        void add(BlockLike from, BlockLike to);
    }
}