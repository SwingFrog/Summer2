package com.swingfrog.summer2.dao.annotation;

import com.swingfrog.summer2.dao.constant.ColumnType;

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
@Target(ElementType.FIELD)
public @interface Column {

    String name() default "";
    String comment() default "";
    ColumnType type() default ColumnType.DEFAULT;
    boolean readOnly() default false;
    int length() default 255;

}
