package net.rebel459.unified.platform;

public class NeoForgePlatformHelper implements PlatformHelper {

    @Override
    public UnifiedRegistries.Items createItems(String modId) {
        return new NeoForgeUnifiedRegistries.Items(modId);
    }

    @Override
    public UnifiedRegistries.Blocks createBlocks(String modId) {
        return new NeoForgeUnifiedRegistries.Blocks(modId);
    }

    @Override
    public UnifiedRegistries.CreativeTabs createCreativeTabs(String modId) {
        return new NeoForgeUnifiedRegistries.CreativeTabs(modId);
    }

    @Override
    public UnifiedRegistries.DataComponentTypes createDataComponentTypes(String modId) {
        return new NeoForgeUnifiedRegistries.DataComponentTypes(modId);
    }

    @Override
    public UnifiedRegistries.ParticleTypes createParticleTypes(String modId) {
        return new NeoForgeUnifiedRegistries.ParticleTypes(modId);
    }

    @Override
    public UnifiedRegistries.MobEffects createMobEffects(String modId) {
        return new NeoForgeUnifiedRegistries.MobEffects(modId);
    }

    @Override
    public UnifiedRegistries.EntityTypes createEntityTypes(String modId) {
        return new NeoForgeUnifiedRegistries.EntityTypes(modId);
    }

    @Override
    public UnifiedRegistries.BlockEntityTypes createBlockEntityTypes(String modId) {
        return new NeoForgeUnifiedRegistries.BlockEntityTypes(modId);
    }

    @Override
    public UnifiedRegistries.SoundEvents createSoundEvents(String modId) {
        return new NeoForgeUnifiedRegistries.SoundEvents(modId);
    }

    @Override
    public UnifiedHelpers.CreativeEntries getCreativeEntries() {
        return new NeoForgeUnifiedHelpers.CreativeEntries();
    }

    @Override
    public UnifiedHelpers.LootTables getLootTables() {
        return new NeoForgeUnifiedHelpers.LootTables();
    }

    @Override
    public UnifiedHelpers.Packs getPacks() {
        return new NeoForgeUnifiedHelpers.Packs();
    }

    @Override
    public UnifiedHelpers.FurnaceFuels getFurnaceFuels() {
        return new NeoForgeUnifiedHelpers.FurnaceFuels();
    }

    @Override
    public UnifiedHelpers.StrippableBlocks getStrippableBlocks() {
        return new NeoForgeUnifiedHelpers.StrippableBlocks();
    }

    @Override
    public UnifiedHelpers.NetworkPayloads getNetworkPayloads() {
        return new NeoForgeUnifiedHelpers.NetworkPayloads();
    }

    @Override
    public UnifiedHelpers.Platform getPlatform() {
        return new NeoForgeUnifiedHelpers.Platform();
    }
}