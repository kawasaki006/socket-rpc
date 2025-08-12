package com.kawasaki.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Limit {
    /*
    * permits how many requests per second
    */
    double permitsPerSecond();

    /*
     * waiting time if failed to get token
     */
    long timeout();
}
