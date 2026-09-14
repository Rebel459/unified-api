package net.rebel459.unified.api.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;

import java.util.function.Function;

public class VanillaSpawnPlacementCodecs {

    private static ExtensibleCodec.Simple<SpawnPlacementType> simple(String path, SpawnPlacementType type) {
        return ExtensibleCodecs.SPAWN_PLACEMENT.register(Identifier.withDefaultNamespace(path), () -> type);
    }

    private static <T> ExtensibleCodec.Complex<SpawnPlacementType, T> complex(String path, MapCodec<T> codec, Function<T, SpawnPlacementType> function) {
        return ExtensibleCodecs.SPAWN_PLACEMENT.register(Identifier.withDefaultNamespace(path), codec, function);
    }

    public static final ExtensibleCodec.Simple<SpawnPlacementType> ON_GROUND = simple("on_ground", SpawnPlacementTypes.ON_GROUND);
    public static final ExtensibleCodec.Simple<SpawnPlacementType> IN_LAVA = simple("in_lava", SpawnPlacementTypes.IN_LAVA);
    public static final ExtensibleCodec.Simple<SpawnPlacementType> IN_WATER = simple("in_water", SpawnPlacementTypes.IN_WATER);
    public static final ExtensibleCodec.Simple<SpawnPlacementType> NO_RESTRICTIONS = simple("no_restrictions", SpawnPlacementTypes.NO_RESTRICTIONS);

    public static void init() {}
}
