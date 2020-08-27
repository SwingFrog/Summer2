package com.swingfrog.summer2.core.ioc;

import com.swingfrog.summer2.core.ioc.annotation.Autowire;
import com.swingfrog.summer2.core.ioc.annotation.Bean;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: toke
 */
public class WaitAutowire {

    private Object target;
    private final Set<Field> waitAutowireFields = new HashSet<>();
    private final Set<Method> waitAutowireMethod = new HashSet<>();
    private final Set<Method> waitCreateBeanMethod = new HashSet<>();

    public static WaitAutowire valueOf(Object target) {
        WaitAutowire waitAutowire = new WaitAutowire();
        waitAutowire.target = target;
        waitAutowire.findWaitAutowire(target.getClass());
        return waitAutowire;
    }

    private void findWaitAutowire(Class<?> targetClass) {
        if (targetClass == null)
            return;
        for (Field field : targetClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowire.class)) {
                waitAutowireFields.add(field);
            }
        }
        for (Method method : targetClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Autowire.class)) {
                waitAutowireMethod.add(method);
            }
            if (method.isAnnotationPresent(Bean.class)) {
                if (method.getParameterCount() > 0) {
                    throw new RuntimeException("bean creator method has parameters");
                }
                waitCreateBeanMethod.add(method);
            }
        }
        findWaitAutowire(targetClass.getSuperclass());
    }

    public Object getTarget() {
        return target;
    }

    public Set<Field> getWaitAutowireFields() {
        return waitAutowireFields;
    }

    public Set<Method> getWaitAutowireMethod() {
        return waitAutowireMethod;
    }

    public Set<Method> getWaitCreateBeanMethod() {
        return waitCreateBeanMethod;
    }

    public boolean isAutowireFinish() {
        return waitAutowireFields.isEmpty() && waitAutowireMethod.isEmpty();
    }

    public boolean isFinish() {
        return isAutowireFinish() && waitCreateBeanMethod.isEmpty();
    }
}
