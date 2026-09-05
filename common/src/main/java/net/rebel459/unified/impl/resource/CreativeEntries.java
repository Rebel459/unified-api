package net.rebel459.unified.impl.resource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.codec.UnifiedCodecs;
import net.rebel459.unified.api.core.UnifiedHelpers;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;

public class CreativeEntries extends SimpleJsonResourceReloadListener<List<CreativeEntries.Definition>> {

    public static final Identifier ID = Unified.id("creative_entries");

    public CreativeEntries() {
        super(UnifiedCodecs.loadRequirements(CreativeEntries.Definition.LIST_CODEC, List::of), FileToIdConverter.json(Unified.MOD_ID + "/" + ID.getPath()));
    }

    @Override
    protected void apply(@NonNull Map<Identifier, List<CreativeEntries.Definition>> map, @NonNull ResourceManager resourceManager, @NonNull ProfilerFiller profilerFiller) {
        for (Map.Entry<Identifier, List<CreativeEntries.Definition>> entry : map.entrySet()) {
            for (CreativeEntries.Definition definition : entry.getValue()) {
                Item[] items = definition.items.toArray(Item[]::new);
                switch (definition.insertion) {
                    case Insertion.Insert ignored -> {
                        UnifiedHelpers.CREATIVE_ENTRIES.insert(definition.tab, items);
                    }
                    case Insertion.After after -> {
                        UnifiedHelpers.CREATIVE_ENTRIES.insertAfter(definition.tab, after.item, items);
                    }
                    case Insertion.Before before -> {
                        UnifiedHelpers.CREATIVE_ENTRIES.insertBefore(definition.tab, before.item, items);
                    }
                }
            }
        }
    }

    public record Definition(ResourceKey<CreativeModeTab> tab, Insertion insertion, List<Item> items) {
        public static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(Registries.CREATIVE_MODE_TAB).fieldOf("tab").forGetter(Definition::tab),
                Insertion.CODEC.forGetter(Definition::insertion),
                ExtraCodecs.compactListCodec(BuiltInRegistries.ITEM.byNameCodec()).fieldOf("items").forGetter(Definition::items)
        ).apply(instance, Definition::new));

        public static Codec<List<Definition>> LIST_CODEC = Codec.list(CODEC).fieldOf("entries").codec();
    }

    public sealed interface Insertion {

        MapCodec<Insertion> CODEC = Type.CODEC.optionalFieldOf("type", Type.INSERT)
                .dispatchMap(Insertion::type, Type::codec);

        Type type();

        record Insert() implements Insertion {
            public static final MapCodec<Insert> CODEC =
                    MapCodec.unit(Insert::new);

            @Override
            public Type type() {
                return Type.INSERT;
            }
        }

        record Before(Item item) implements Insertion {
            public static final MapCodec<Before> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("target").forGetter(Before::item)
            ).apply(instance, Before::new));

            @Override
            public Type type() {
                return Type.INSERT_BEFORE;
            }
        }

        record After(Item item) implements Insertion {
            public static final MapCodec<After> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("target").forGetter(After::item)
            ).apply(instance, After::new));

            @Override
            public Type type() {
                return Type.INSERT_AFTER;
            }
        }

        enum Type implements StringRepresentable {
            INSERT("insert", Insert.CODEC),
            INSERT_BEFORE("insert_before", Before.CODEC),
            INSERT_AFTER("insert_after", After.CODEC);

            public static final Codec<Type> CODEC =
                    StringRepresentable.fromEnum(Type::values);

            private final String name;
            private final MapCodec<? extends Insertion> codec;

            Type(String name, MapCodec<? extends Insertion> codec) {
                this.name = name;
                this.codec = codec;
            }

            public MapCodec<? extends Insertion> codec() {
                return codec;
            }

            @Override
            public String getSerializedName() {
                return name;
            }
        }
    }
}
