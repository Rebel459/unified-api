package net.rebel459.unified.platform;

public class FabricPlatformHelper implements PlatformHelper {

    @Override
    public UnifiedRegistries.Items createItems(String modId) {
        return new FabricUnifiedRegistries.Items(modId);
    }

    @Override
    public UnifiedRegistries.Blocks createBlocks(String modId) {
        return new FabricUnifiedRegistries.Blocks(modId);
    }

    @Override
    public UnifiedRegistries.CreativeTabs createCreativeTabs(String modId) {
        return new FabricUnifiedRegistries.CreativeTabs(modId);
    }

    @Override
    public UnifiedRegistries.DataComponentTypes createDataComponentTypes(String modId) {
        return new FabricUnifiedRegistries.DataComponentTypes(modId);
    }

    @Override
    public UnifiedRegistries.ParticleTypes createParticleTypes(String modId) {
        return new FabricUnifiedRegistries.ParticleTypes(modId);
    }

    @Override
    public UnifiedRegistries.MobEffects createMobEffects(String modId) {
        return new FabricUnifiedRegistries.MobEffects(modId);
    }

    @Override
    public UnifiedRegistries.EntityTypes createEntityTypes(String modId) {
        return new FabricUnifiedRegistries.EntityTypes(modId);
    }

    @Override
    public UnifiedRegistries.SoundEvents createSoundEvents(String modId) {
        return new FabricUnifiedRegistries.SoundEvents(modId);
    }

    @Override
    public UnifiedHelpers.CreativeEntries getCreativeEntries() {
        return new FabricUnifiedHelpers.CreativeEntries();
    }

    @Override
    public UnifiedHelpers.LootTables getLootTables() {
        return new FabricUnifiedHelpers.LootTables();
    }

    @Override
    public UnifiedHelpers.Packs getPacks() {
        return new FabricUnifiedHelpers.Packs();
    }

    @Override
    public UnifiedHelpers.FurnaceFuels getFurnaceFuels() {
        return new FabricUnifiedHelpers.FurnaceFuels();
    }

    @Override
    public UnifiedHelpers.StrippableBlocks getStrippableBlocks() {
        return new FabricUnifiedHelpers.StrippableBlocks();
    }

    @Override
    public UnifiedHelpers.NetworkPayloads getNetworkPayloads() {
        return new FabricUnifiedHelpers.NetworkPayloads();
    }

    @Override
    public UnifiedHelpers.Platform getPlatform() {
        return new FabricUnifiedHelpers.Platform();
    }
}