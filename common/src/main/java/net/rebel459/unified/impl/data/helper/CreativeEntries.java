package net.rebel459.unified.impl.data.helper;

import com.mojang.datafixers.util.Either;
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
import net.minecraft.world.item.ItemStack;
import net.rebel459.unified.Unified;
import net.rebel459.unified.api.codec.UnifiedCodecs;
import net.rebel459.unified.api.core.UnifiedEvents;
import net.rebel459.unified.api.event.CreativeEntryContext;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreativeEntries extends SimpleJsonResourceReloadListener<List<CreativeEntries.Definition>> {

    public static final Identifier ID = Unified.id("creative_entries");
    private static final Codec<Either<Item, ItemStack>> ENTRY_CODEC = Codec.either(
            BuiltInRegistries.ITEM.byNameCodec(),
            ItemStack.CODEC
    );

    private volatile List<Definition> definitions = List.of();

    public CreativeEntries() {
        super(UnifiedCodecs.loadRequirements(CreativeEntries.Definition.LIST_CODEC, List::of), FileToIdConverter.json(Unified.MOD_ID + "/" + ID.getPath()));
        UnifiedEvents.CreativeEntries.modify(this::modify);
    }

    @Override
    protected void apply(@NonNull Map<Identifier, List<CreativeEntries.Definition>> map, @NonNull ResourceManager resourceManager, @NonNull ProfilerFiller profilerFiller) {
        List<Definition> loaded = new ArrayList<>();
        for (List<Definition> entries : map.values()) {
            loaded.addAll(entries);
        }
        definitions = List.copyOf(loaded);
    }

    private void modify(ResourceKey<CreativeModeTab> tab, CreativeEntryContext context) {
        for (Definition definition : definitions) {
            if (!definition.tab.equals(tab)) {
                continue;
            }

            ItemStack[] items = definition.items.stream()
                    .map(entry -> entry.map(Item::getDefaultInstance, ItemStack::copy))
                    .toArray(ItemStack[]::new);

            switch (definition.insertion) {
                case Insertion.Insert ignored -> context.insert(items);
                case Insertion.After after -> {
                    after.target.left().ifPresent(item -> context.insertAfter(item, items));
                    after.target.right().ifPresent(stack -> context.insertAfter(stack, items));
                }
                case Insertion.Before before -> {
                    before.target.left().ifPresent(item -> context.insertBefore(item, items));
                    before.target.right().ifPresent(stack -> context.insertBefore(stack, items));
                }
            }
        }
    }

    public record Definition(ResourceKey<CreativeModeTab> tab, Insertion insertion, List<Either<Item, ItemStack>> items) {
        public static final Codec<Definition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(Registries.CREATIVE_MODE_TAB).fieldOf("tab").forGetter(Definition::tab),
                Insertion.CODEC.forGetter(Definition::insertion),
                ExtraCodecs.compactListCodec(ENTRY_CODEC).fieldOf("items").forGetter(Definition::items)
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

        record Before(Either<Item, ItemStack> target) implements Insertion {
            public static final MapCodec<Before> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ENTRY_CODEC.fieldOf("target").forGetter(Before::target)
            ).apply(instance, Before::new));

            @Override
            public Type type() {
                return Type.INSERT_BEFORE;
            }
        }

        record After(Either<Item, ItemStack> target) implements Insertion {
            public static final MapCodec<After> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ENTRY_CODEC.fieldOf("target").forGetter(After::target)
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
