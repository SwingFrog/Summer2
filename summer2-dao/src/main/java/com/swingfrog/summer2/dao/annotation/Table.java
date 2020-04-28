package com.swingfrog.summer2.dao.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: toke
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Table {

    String name() default "";
    String comment() default "";
    String charset() default "utf8mb4";
    String collate() default "utf8mb4_general_ci";
    Index[] index() default {};

}
