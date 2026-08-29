package net.rebel459.unified.impl.client.helper;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Music;
import net.minecraft.tags.TagKey;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.HashMap;
import java.util.Map;

public class StructureMusicImpl {
    public static Map<ResourceKey<Structure>, MusicAndRequirement> STRUCTURE_MUSIC = new HashMap<>();
    public static Map<TagKey<Structure>, MusicAndRequirement> STRUCTURE_TAG_MUSIC = new HashMap<>();

    public record MusicAndRequirement(BackgroundMusic music, boolean useFulLBox) {}
}
