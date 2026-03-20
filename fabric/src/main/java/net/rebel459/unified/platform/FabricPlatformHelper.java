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
    public UnifiedRegistries.BlockEntityTypes createBlockEntityTypes(String modId) {
        return new FabricUnifiedRegistries.BlockEntityTypes(modId);
    }

    @Override
    public UnifiedRegistries.SoundEvents createSoundEvents(String modId) {
        return new FabricUnifiedRegistries.SoundEvents(modId);
    }

    @Override
    public HelpersImpl.CreativeEntries getCreativeEntries() {
        return new FabricHelpersImpl.CreativeEntries();
    }

    @Override
    public HelpersImpl.LootTables getLootTables() {
        return new FabricHelpersImpl.LootTables();
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
        return new FabricHelpersImpl.Platform();
    }
}