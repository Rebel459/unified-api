package net.rebel459.unified.api.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.AgeableWaterCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.squid.GlowSquid;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.rebel459.unified.api.codec.ExtensibleCodec;
import net.rebel459.unified.api.codec.ExtensibleCodecs;
import net.rebel459.unified.api.codec.ExtensibleSpawnPredicate;

public final class VanillaSpawnPredicateCodecs {
    private VanillaSpawnPredicateCodecs() {}

    private static <T extends Entity> ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> simple(
            String path,
            SpawnPlacements.SpawnPredicate<T> predicate
    ) {
        return ExtensibleCodecs.SPAWN_PREDICATE.registerSpawnPredicate(
                Identifier.withDefaultNamespace(path),
                () -> predicate
        );
    }

    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> ANIMAL = simple("animal", Animal::checkAnimalSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> ANY_LIGHT_MONSTER = simple("any_light_monster", Monster::checkAnyLightMonsterSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> ARMADILLO = simple("armadillo", Armadillo::checkArmadilloSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> AXOLOTL = simple("axolotl", Axolotl::checkAxolotlSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> BAT = simple("bat", Bat::checkBatSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> CAMEL = simple("camel", Camel::checkCamelSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> DROWNED = simple("drowned", Drowned::checkDrownedSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> ENDERMITE = simple("endermite", Endermite::checkEndermiteSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> FOX = simple("fox", Fox::checkFoxSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> FROG = simple("frog", Frog::checkFrogSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> GHAST = simple("ghast", Ghast::checkGhastSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> GLOW_SQUID = simple("glow_squid", GlowSquid::checkGlowSquidSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> GOAT = simple("goat", Goat::checkGoatSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> GUARDIAN = simple("guardian", Guardian::checkGuardianSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> HOGLIN = simple("hoglin", Hoglin::checkHoglinSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> MAGMA_CUBE = simple("magma_cube", MagmaCube::checkMagmaCubeSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> MOB = simple("mob", Mob::checkMobSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> MONSTER = simple("monster", Monster::checkMonsterSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> MUSHROOM = simple("mushroom", MushroomCow::checkMushroomSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> NAUTILUS = simple("nautilus", AbstractNautilus::checkNautilusSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> OCELOT = simple("ocelot", Ocelot::checkOcelotSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> PARROT = simple("parrot", Parrot::checkParrotSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> PATROLLING_MONSTER = simple("patrolling_monster", PatrollingMonster::checkPatrollingMonsterSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> PIGLIN = simple("piglin", Piglin::checkPiglinSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> POLAR_BEAR = simple("polar_bear", PolarBear::checkPolarBearSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> RABBIT = simple("rabbit", Rabbit::checkRabbitSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> SILVERFISH = simple("silverfish", Silverfish::checkSilverfishSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> SKELETON_HORSE = simple("skeleton_horse", SkeletonHorse::checkSkeletonHorseSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> SLIME = simple("slime", Slime::checkSlimeSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> STRAY = simple("stray", Stray::checkStraySpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> STRIDER = simple("strider", Strider::checkStriderSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> SURFACE_AGEABLE_WATER_CREATURE = simple("surface_ageable_water_creature", AgeableWaterCreature::checkSurfaceAgeableWaterCreatureSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> SURFACE_MONSTER = simple("surface_monster", Monster::checkSurfaceMonstersSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> SURFACE_WATER_ANIMAL = simple("surface_water_animal", WaterAnimal::checkSurfaceWaterAnimalSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> TROPICAL_FISH = simple("tropical_fish", TropicalFish::checkTropicalFishSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> TURTLE = simple("turtle", Turtle::checkTurtleSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> WOLF = simple("wolf", Wolf::checkWolfSpawnRules);
    public static final ExtensibleCodec.Simple<ExtensibleSpawnPredicate.Type> ZOMBIFIED_PIGLIN = simple("zombified_piglin", ZombifiedPiglin::checkZombifiedPiglinSpawnRules);

    public static void init() {}
}
