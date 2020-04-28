package com.swingfrog.summer2.dao.annotation;

import com.swingfrog.summer2.dao.constant.IndexType;

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
public @interface Index {

    String name() default "";
    String[] fields();
    IndexType type() default IndexType.NORMAL;

}
