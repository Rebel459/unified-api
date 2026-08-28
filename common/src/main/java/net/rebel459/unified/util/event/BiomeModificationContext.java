package net.rebel459.unified.util.event;

import net.rebel459.unified.platform.HelpersImpl;

public abstract class BiomeModificationContext {

    protected BiomeModificationContext() {}

    public abstract Worldgen getFeatures();
    public abstract Effects getEffects();
    public abstract Climate getClimate();
    public abstract EnvironmentAttributes getEnvironmentAttributes();
    public abstract MobSpawns getMobSpawns();

    public interface Worldgen extends HelpersImpl.BiomeModifications.Worldgen {}
    public interface Effects extends HelpersImpl.BiomeModifications.Effects {}
    public interface Climate extends HelpersImpl.BiomeModifications.Climate {}
    public interface EnvironmentAttributes extends HelpersImpl.BiomeModifications.EnvironmentAttributes {}
    public interface MobSpawns extends HelpersImpl.BiomeModifications.MobSpawns {}
}