package net.rebel459.unified.platform;

public class NeoForgeInternalHelper implements InternalHelper {

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
    public UnifiedRegistries.EnchantmentCodecs createEnchantmentCodecs(String modId) {
        return new NeoForgeUnifiedRegistries.EnchantmentCodecs(modId);
    }

    @Override
    public UnifiedRegistries.MapDecorationTypes createMapDecorationTypes(String modId) {
        return new NeoForgeUnifiedRegistries.MapDecorationTypes(modId);
    }

    @Override
    public HelpersImpl.CreativeEntries getCreativeEntries() {
        return new NeoForgeHelpersImpl.CreativeEntries();
    }

    @Override
    public HelpersImpl.Packs getPacks() {
        return new NeoForgeHelpersImpl.Packs();
    }

    @Override
    public HelpersImpl.Networking getNetworkPayloads() {
        return new NeoForgeHelpersImpl.Networking();
    }

    @Override
    public HelpersImpl.Platform getPlatform() {
        return new NeoForgeUnifiedPlatform();
    }

    @Override
    public HelpersImpl.BiomeModifications getBiomeModifications() {
        return new NeoForgeHelpersImpl.BiomeModifications();
    }
}