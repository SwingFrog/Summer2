package com.swingfrog.summer2.core.ioc;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @author: toke
 */
public interface Container {

    <T> T getBean(Class<T> targetClass);
    <T> List<T> listBean(Class<T> targetClass);
    List<Object> listBeanByAnnotation(Class<? extends Annotation> targetAnnotation);

}
