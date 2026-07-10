package net.rebel459.unified.util.helper;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.WeatheringCopperCollection;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.rebel459.unified.platform.InternalHandlerImpl;
import net.rebel459.unified.util.BlockLike;
import net.rebel459.unified.util.helper.impl.BlockConversionsImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface BlockConversions {

    default void addStrippable(BlockLike originalBlock, BlockLike convertedBlock) {
        add(stack -> stack.getItem() instanceof AxeItem, originalBlock, convertedBlock, SoundEvents.AXE_STRIP);
    }

    default void addWeathering(BlockLike unaffected, BlockLike exposed, BlockLike weathered, BlockLike oxidized, BlockLike waxed, BlockLike waxedExposed, BlockLike waxedWeathered, BlockLike waxedOxidized) {
        List<Pair<BlockLike, BlockLike>> waxPairs = List.of(Pair.of(unaffected, waxed), Pair.of(exposed, waxedExposed), Pair.of(weathered, waxedWeathered), Pair.of(oxidized, waxedOxidized));
        List<Pair<BlockLike, BlockLike>> oxidizationPairs = List.of(Pair.of(oxidized, weathered), Pair.of(weathered, exposed), Pair.of(exposed, unaffected));
        for (Pair<BlockLike, BlockLike> pair : waxPairs) {
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
        for (Pair<BlockLike, BlockLike> pair : oxidizationPairs) {
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
        BlockConversionsImpl.Oxidizables oxidizables = InternalHandlerImpl.INSTANCE.impl().getOxidizables();
        oxidizables.add(unaffected, exposed);
        oxidizables.add(exposed, weathered);
        oxidizables.add(weathered, oxidized);
    }

    default void add(Predicate<ItemStack> item, BlockLike originalBlock, BlockLike convertedBlock, SoundEvent sound) {
        add(item, originalBlock, convertedBlock, sound, 1F, 1F);
    }
    default void add(Predicate<ItemStack> item, BlockLike originalBlock, BlockLike convertedBlock, SoundEvent sound, float volume, float pitch) {
        BlockConversionsImpl.INTERACTIONS.computeIfAbsent(originalBlock.asBlock(), _ -> new ArrayList<>()).add(new BlockConversionsImpl.Record(item, convertedBlock.asBlock(), (context) -> {
            Player player = context.getPlayer();
            if (player == null) return;
            context.getLevel().playSound(player, context.getClickedPos(), sound, SoundSource.BLOCKS, volume, pitch);
            context.getItemInHand().hurtAndBreak(1, player, player.getEquipmentSlotForItem(context.getItemInHand()));
        }));
    }
    default void add(Predicate<ItemStack> item, BlockLike originalBlock, BlockLike convertedBlock, Consumer<UseOnContext> context) {
        BlockConversionsImpl.INTERACTIONS.computeIfAbsent(originalBlock.asBlock(), _ -> new ArrayList<>()).add(new BlockConversionsImpl.Record(item, convertedBlock.asBlock(), context));
    }
}
