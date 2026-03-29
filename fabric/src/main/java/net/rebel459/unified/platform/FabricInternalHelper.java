package net.rebel459.unified.platform;

public class FabricInternalHelper implements InternalHelper {

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
    public UnifiedRegistries.BlockEntityTypes createBlockEntityTypes(String modId) {
        return new FabricUnifiedRegistries.BlockEntityTypes(modId);
    }

    @Override
    public UnifiedRegistries.SoundEvents createSoundEvents(String modId) {
        return new FabricUnifiedRegistries.SoundEvents(modId);
    }

    @Override
    public UnifiedRegistries.EnchantmentCodecs createEnchantmentEffects(String modId) {
        return new FabricUnifiedRegistries.EnchantmentCodecs(modId);
    }

    @Override
    public HelpersImpl.CreativeEntries getCreativeEntries() {
        return new FabricHelpersImpl.CreativeEntries();
    }

    @Override
    public HelpersImpl.Packs getPacks() {
        return new FabricHelpersImpl.Packs();
    }

    @Override
    public HelpersImpl.Networking getNetworkPayloads() {
        return new FabricHelpersImpl.Networking();
    }

    @Override
    public HelpersImpl.Platform getPlatform() {
        return new FabricUnifiedPlatform();
    }

    @Override
    public HelpersImpl.BiomeModifications getBiomeModifications() {
        return new FabricHelpersImpl.BiomeModifications();
    }
}