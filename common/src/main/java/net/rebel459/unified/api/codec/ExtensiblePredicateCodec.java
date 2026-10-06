package net.rebel459.unified.api.codec;

import com.mojang.serialization.Codec;
import net.rebel459.unified.Unified;

import java.util.List;
import java.util.function.Predicate;

public class ExtensiblePredicateCodec<P> extends ExtensibleCodec<P> {
    private final Simple<P> always;
    private final Simple<P> never;
    private final Complex<P, List<Entry<P>>> allOf;
    private final Complex<P, List<Entry<P>>> anyOf;
    private final Complex<P, Entry<P>> not;

    public Simple<P> always() {
        return always;
    }

    public Simple<P> never() {
        return never;
    }

    public Complex<P, List<Entry<P>>> allOf() {
        return allOf;
    }

    public Complex<P, List<Entry<P>>> anyOf() {
        return anyOf;
    }

    public Complex<P, Entry<P>> not() {
        return not;
    }

    public ExtensiblePredicateCodec(Adapter<P> adapter) {
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

    public static <T> ExtensiblePredicateCodec<Predicate<T>> predicate() {
        return new ExtensiblePredicateCodec<>(evaluation -> value -> evaluation.evaluate(predicate -> predicate.test(value)));
    }

    public static ExtensiblePredicateCodec<Boolean> booleanValue() {
        return new ExtensiblePredicateCodec<>(evaluation -> evaluation.evaluate(Boolean::booleanValue));
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
