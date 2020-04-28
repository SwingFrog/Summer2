package com.swingfrog.summer2.core.ioc;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * @author: toke
 */
public interface Container {

    <T> T getBean(Class<T> targetClass);
    <T> Set<T> listBean(Class<T> targetClass);
    Set<Object> listBeanByAnnotation(Class<? extends Annotation> targetAnnotation);

}
