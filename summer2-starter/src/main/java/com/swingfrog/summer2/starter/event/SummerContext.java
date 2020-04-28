package com.swingfrog.summer2.starter.event;

import com.swingfrog.summer2.core.ioc.Container;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * @author: toke
 */
public class SummerContext implements Container {

    private final Container container;

    public SummerContext(Container container) {
        this.container = container;
    }

    @Override
    public <T> T getBean(Class<T> targetClass) {
        return container.getBean(targetClass);
    }

    @Override
    public <T> Set<T> listBean(Class<T> targetClass) {
        return container.listBean(targetClass);
    }

    @Override
    public Set<Object> listBeanByAnnotation(Class<? extends Annotation> targetAnnotation) {
        return container.listBeanByAnnotation(targetAnnotation);
    }

}
