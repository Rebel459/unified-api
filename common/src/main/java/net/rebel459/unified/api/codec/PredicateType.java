package net.rebel459.unified.api.codec;

import com.mojang.serialization.Codec;
import net.rebel459.unified.Unified;

import java.util.List;
import java.util.function.Predicate;

public class PredicateType<P> extends ExtensibleCodec<P> {
    public final Simple<P> always;
    public final Simple<P> never;
    public final Complex<P, List<Entry<P>>> allOf;
    public final Complex<P, List<Entry<P>>> anyOf;
    public final Complex<P, Entry<P>> not;

    public PredicateType(Adapter<P> adapter) {
        super();
        always = register(Unified.id("always"), () -> adapter.adapt(_ -> true));
        never = register(Unified.id("never"), () -> adapter.adapt(_ -> false));
        Codec<Entry<P>> nested = Codec.lazyInitialized(this::codec);
        allOf = register(Unified.id("all_of"), nested.listOf().fieldOf("predicates"), entries ->
                adapter.adapt(test -> entries.stream().allMatch(entry -> test.test(entry.get()))));
        anyOf = register(Unified.id("any_of"), nested.listOf().fieldOf("predicates"), entries ->
                adapter.adapt(test -> entries.stream().anyMatch(entry -> test.test(entry.get()))));
        not = register(Unified.id("not"), nested.fieldOf("predicate"), entry ->
                adapter.adapt(test -> !test.test(entry.get())));
    }

    public static <T> PredicateType<Predicate<T>> predicate() {
        return new PredicateType<>(evaluation -> value -> evaluation.evaluate(predicate -> predicate.test(value)));
    }

    public static PredicateType<Boolean> booleanValue() {
        return new PredicateType<>(evaluation -> evaluation.evaluate(Boolean::booleanValue));
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
