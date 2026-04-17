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
import net.minecraft.world.level.block.WeatheringCopperBlocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.rebel459.unified.platform.InternalHandlerImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface BlockConversions {

    default void addStrippable(Block originalBlock, Block convertedBlock) {
        add(stack -> stack.getItem() instanceof AxeItem, originalBlock, convertedBlock, SoundEvents.AXE_STRIP);
    }

    @Deprecated
    default void addWaxed(Block block, Block waxedBlock, Block exposedBlock, Block waxedExposedBlock, Block weatheredBlock, Block waxedWeatheredBlock, Block oxidizedBlock, Block waxedOxidizedBlock) {
        addWeathering(new WeatheringCopperBlocks(block, exposedBlock, weatheredBlock, oxidizedBlock, waxedBlock, waxedExposedBlock, waxedWeatheredBlock, waxedOxidizedBlock));
    }

    default void addWeathering(WeatheringCopperBlocks set) {
        Block unaffected = set.unaffected();
        Block exposed = set.exposed();
        Block weathered = set.weathered();
        Block oxidized = set.oxidized();
        Block waxed = set.waxed();
        Block waxedExposed = set.waxedExposed();
        Block waxedWeathered = set.waxedWeathered();
        Block waxedOxidized = set.waxedOxidized();
        List<Pair<Block, Block>> waxPairs = List.of(Pair.of(unaffected, waxed), Pair.of(exposed, waxedExposed), Pair.of(weathered, waxedWeathered), Pair.of(oxidized, waxedOxidized));
        List<Pair<Block, Block>> oxidizationPairs = List.of(Pair.of(oxidized, weathered), Pair.of(weathered, exposed), Pair.of(exposed, unaffected));
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
        BlockConversionsImpl.Oxidizables oxidizables = InternalHandlerImpl.INSTANCE.impl().getOxidizables();
        oxidizables.add(unaffected, exposed);
        oxidizables.add(exposed, weathered);
        oxidizables.add(weathered, oxidized);
    }

    default void add(Predicate<ItemStack> item, Block originalBlock, Block convertedBlock, SoundEvent sound) {
        add(item, originalBlock, convertedBlock, sound, 1F, 1F);
    }
    default void add(Predicate<ItemStack> item, Block originalBlock, Block convertedBlock, SoundEvent sound, float volume, float pitch) {
        BlockConversionsImpl.INTERACTIONS.computeIfAbsent(originalBlock, _ -> new ArrayList<>()).add(new BlockConversionsImpl.Record(item, convertedBlock, (context) -> {
            Player player = context.getPlayer();
            if (player == null) return;
            context.getLevel().playSound(player, context.getClickedPos(), sound, SoundSource.BLOCKS, volume, pitch);
            context.getItemInHand().hurtAndBreak(1, player, player.getEquipmentSlotForItem(context.getItemInHand()));
        }));
    }
    default void add(Predicate<ItemStack> item, Block originalBlock, Block convertedBlock, Consumer<UseOnContext> context) {
        BlockConversionsImpl.INTERACTIONS.computeIfAbsent(originalBlock, _ -> new ArrayList<>()).add(new BlockConversionsImpl.Record(item, convertedBlock, context));
    }
}
