package org.granary.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD})
public @interface GuardedBy
{
    /**
     * The monitor object guarding the annotated field.
     *
     * @return The name of the monitor object which guards this field.
     */
    String value();
}
