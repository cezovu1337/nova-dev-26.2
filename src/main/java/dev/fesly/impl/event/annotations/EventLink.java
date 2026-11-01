package dev.fesly.impl.event.annotations;
import dev.fesly.impl.event.Priorities;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface EventLink {
    byte value() default Priorities.MEDIUM;
}
