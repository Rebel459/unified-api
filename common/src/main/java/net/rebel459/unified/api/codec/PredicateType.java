package net.rebel459.unified.api.codec;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.rebel459.unified.Unified;

import java.util.List;
import java.util.function.Predicate;

public class PredicateType<P> extends ExtensibleCodec<P> {
    public final Simple<P> ALWAYS;
    public final Simple<P> NEVER;
    public final Complex<P, List<Entry<P>>> ALL_OF;
    public final Complex<P, List<Entry<P>>> ANY_OF;
    public final Complex<P, Entry<P>> NOT;

    public PredicateType(Adapter<P> adapter) {
        super();
        ALWAYS = register(Unified.id("always"), () -> adapter.adapt(_ -> true));
        NEVER = register(Unified.id("never"), () -> adapter.adapt(_ -> false));
        Codec<Entry<P>> nested = Codec.lazyInitialized(this::codec);
        ALL_OF = register(Unified.id("all_of"), nested.listOf().fieldOf("predicates"), entries -> {
            List<P> predicates = entries.stream().map(Entry::get).toList();
            return adapter.adapt(test -> predicates.stream().allMatch(test));
        });
        ANY_OF = register(Unified.id("any_of"), nested.listOf().fieldOf("predicates"), entries -> {
            List<P> predicates = entries.stream().map(Entry::get).toList();
            return adapter.adapt(test -> predicates.stream().anyMatch(test));
        });
        NOT = register(Unified.id("not"), nested.fieldOf("predicate"), entry -> {
            P predicate = entry.get();
            return adapter.adapt(test -> !test.test(predicate));
        });
    }

    public static <T> PredicateType<Predicate<T>> predicate() {
        return new PredicateType<>(evaluation -> value -> evaluation.evaluate(predicate -> predicate.test(value)));
    }

    @FunctionalInterface
    public interface Adapter<P> {
        P adapt(Evaluation<P> evaluation);
    }

    @FunctionalInterface
    public interface Evaluation<P> {
        boolean evaluate(Predicate<P> test);
    }
}
