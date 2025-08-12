package com.kawasaki.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Retry {
    // get exception type for retry
    Class<? extends  Throwable> value() default Exception.class;

    int maxAttempts() default 3;

    int delay() default 0;
}
