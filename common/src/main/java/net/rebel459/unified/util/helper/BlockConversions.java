package net.rebel459.unified.util.helper;

import com.mojang.datafixers.util.Pair;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface BlockConversions {

    default void addStrippable(Block originalBlock, Block convertedBlock) {
        add(stack -> stack.getItem() instanceof AxeItem, originalBlock, convertedBlock, SoundEvents.AXE_STRIP);
    }

    default void addWaxed(Block block, Block waxedBlock, Block exposedBlock, Block waxedExposedBlock, Block weatheredBlock, Block waxedWeatheredBlock, Block oxidizedBlock, Block waxedOxidizedBlock) {
        List<Pair<Block, Block>> waxPairs = List.of(Pair.of(block, waxedBlock), Pair.of(exposedBlock, waxedExposedBlock), Pair.of(weatheredBlock, waxedWeatheredBlock), Pair.of(oxidizedBlock, waxedOxidizedBlock));
        List<Pair<Block, Block>> oxidizationPairs = List.of(Pair.of(oxidizedBlock, weatheredBlock), Pair.of(weatheredBlock, exposedBlock), Pair.of(exposedBlock, block));
        for (Pair<Block, Block> pair : waxPairs) {
            add(stack -> stack.getItem() instanceof HoneycombItem, pair.getFirst(), pair.getSecond(), (context -> {
                Player player = context.getPlayer();
                Level level = context.getLevel();
                BlockPos pos = context.getClickedPos();
                BlockState oldState = level.getBlockState(pos);
                if (player == null) return;
                context.getItemInHand().shrink(1);
                level.levelEvent(player, 3003, pos, 0);
                if (oldState.getBlock() instanceof ChestBlock && oldState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                    BlockPos neighborPos = ChestBlock.getConnectedBlockPos(pos, oldState);
                    level.gameEvent(GameEvent.BLOCK_CHANGE, neighborPos, GameEvent.Context.of(player, level.getBlockState(neighborPos)));
                    level.levelEvent(player, 3003, neighborPos, 0);
                }
            }));
            add(stack -> stack.getItem() instanceof AxeItem, pair.getSecond(), pair.getFirst(), (context) -> {
                Player player = context.getPlayer();
                Level level = context.getLevel();
                BlockPos pos = context.getClickedPos();
                BlockState oldState = level.getBlockState(pos);
                if (player == null) return;
                AxeItem.spawnSoundAndParticle(level, pos, player, oldState, SoundEvents.AXE_WAX_OFF, 3004);
                context.getItemInHand().hurtAndBreak(1, player, player.getEquipmentSlotForItem(context.getItemInHand()));
            });
        }
        for (Pair<Block, Block> pair : oxidizationPairs) {
            add(stack -> stack.getItem() instanceof AxeItem, pair.getFirst(), pair.getSecond(), (context) -> {
                Player player = context.getPlayer();
                Level level = context.getLevel();
                BlockPos pos = context.getClickedPos();
                BlockState oldState = level.getBlockState(pos);
                if (player == null) return;
                AxeItem.spawnSoundAndParticle(level, pos, player, oldState, SoundEvents.AXE_SCRAPE, 3005);
                context.getItemInHand().hurtAndBreak(1, player, player.getEquipmentSlotForItem(context.getItemInHand()));
            });
        }
    }

    default void add(Predicate<ItemStack> item, Block originalBlock, Block convertedBlock, SoundEvent sound) {
        add(item, originalBlock, convertedBlock, sound, 1F, 1F);
    }
    default void add(Predicate<ItemStack> item, Block originalBlock, Block convertedBlock, SoundEvent sound, float volume, float pitch) {
        Impl.INTERACTIONS.put(originalBlock, new Impl.Record(item, convertedBlock, (context) -> {
            Player player = context.getPlayer();
            if (player == null) return;
            context.getLevel().playSound(player, context.getClickedPos(), sound, SoundSource.BLOCKS, volume, pitch);
            context.getItemInHand().hurtAndBreak(1, player, player.getEquipmentSlotForItem(context.getItemInHand()));
        }));
    }
    default void add(Predicate<ItemStack> item, Block originalBlock, Block convertedBlock, Consumer<UseOnContext> context) {
        Impl.INTERACTIONS.put(originalBlock, new Impl.Record(item, convertedBlock, context));
    }

    class Impl {
        public static InteractionResult useOn(UseOnContext context) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            Player player = context.getPlayer();
            if (AxeItem.playerHasBlockingItemUseIntent(context)) {
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

        private static Optional<BlockState> evaluateNewBlockState(UseOnContext context, BlockState oldState) {
            var interaction = INTERACTIONS.get(oldState.getBlock());
            if (context.getPlayer() == null || interaction == null || !interaction.validItem.test(context.getItemInHand()))
                return Optional.empty();
            interaction.context.accept(context);
            return Optional.of(interaction.convertedBlock.withPropertiesOf(oldState));
        }

        record Record(Predicate<ItemStack> validItem, Block convertedBlock, Consumer<UseOnContext> context) {}

        static HashMap<Block, Record> INTERACTIONS = new HashMap<>();
    }
}
