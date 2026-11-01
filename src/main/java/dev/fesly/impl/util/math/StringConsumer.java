package dev.fesly.impl.util.math;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
@FunctionalInterface
public interface StringConsumer extends Consumer<String>, DoubleConsumer {
    void accept(String t);
    @Deprecated
    @Override
    default void accept(double t) {
        accept(String.valueOf(t));
    }
    @Deprecated
    default void accept(Double t) {
        this.accept(t.doubleValue());
    }
    default StringConsumer andThen(final StringConsumer after) {
        Objects.requireNonNull(after);
        return (String t) -> {
            accept(t);
            after.accept(t);
        };
    }
    @Deprecated
    @Override
    default StringConsumer andThen(final DoubleConsumer after) {
        Objects.requireNonNull(after);
        return (String t) -> {
            accept(t);
            after.accept(Double.parseDouble(t));
        };
    }
    @Deprecated
    @Override
    default Consumer<String> andThen(final Consumer<? super String> after) {
        return Consumer.super.andThen(after);
    }
}
