package com.swingfrog.summer2.core.ioc;

import com.swingfrog.summer2.core.ioc.annotation.Autowire;
import com.swingfrog.summer2.core.ioc.annotation.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author: toke
 */
public class IocProcessor implements Container {
    
    private static final Logger log = LoggerFactory.getLogger(IocProcessor.class);

    private Set<Class<?>> registers = ConcurrentHashMap.newKeySet();
    private Set<WaitAutowire> waitAutowire = ConcurrentHashMap.newKeySet();
    private final Set<Object> beans = ConcurrentHashMap.newKeySet();
    private final ConcurrentMap<Class<? extends Annotation>, Set<Annotation>> excludeAnnotation = new ConcurrentHashMap<>();

    public void scanComponent(String pack) {
        Objects.requireNonNull(pack);
        ClassScanner.findClasses(pack).stream()
                .filter(targetClass -> !targetClass.isInterface())
                .filter(targetClass -> !targetClass.isAnnotation())
                .filter(targetClass -> hasAnnotation(targetClass, Component.class))
                .forEach(registers::add);
    }

    public void registerClass(Class<?> targetClass) {
        Objects.requireNonNull(targetClass);
        if (targetClass.isInterface())
            throw new IocRuntimeException("target class can not be interface -> " + targetClass.getName());
        if (targetClass.isAnnotation())
            throw new IocRuntimeException("target class can not be annotation -> " + targetClass.getName());
        registers.add(targetClass);
    }

    public void registerBean(Object bean) {
        Objects.requireNonNull(bean);
        waitAutowire.add(WaitAutowire.valueOf(bean));
    }

    @Override
    public <T> T getBean(Class<T> targetClass) {
        Objects.requireNonNull(targetClass);
        return beans.stream()
                .filter(targetClass::isInstance)
                .map(targetClass::cast)
                .findAny()
                .orElse(null);
    }

    @Override
    public <T> Set<T> listBean(Class<T> targetClass) {
        Objects.requireNonNull(targetClass);
        return beans.stream()
                .filter(targetClass::isInstance)
                .map(targetClass::cast)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Object> listBeanByAnnotation(Class<? extends Annotation> targetAnnotation) {
        Objects.requireNonNull(targetAnnotation);
        return beans.stream()
                .filter(bean -> hasAnnotation(bean.getClass(), targetAnnotation))
                .collect(Collectors.toSet());
    }

    private boolean hasAnnotation(Class<?> targetClass, Class<? extends Annotation> targetAnnotation) {
        if (targetClass.isAnnotationPresent(targetAnnotation))
            return true;
        for (Annotation annotation : targetClass.getDeclaredAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(targetAnnotation)) {
                return true;
            }
            if (!excludeAnnotation.computeIfAbsent(targetAnnotation, k -> ConcurrentHashMap.newKeySet()).add(annotation))
                continue;
            if (hasAnnotation(annotation.annotationType(), targetAnnotation)) {
                excludeAnnotation.get(targetAnnotation).remove(annotation);
                return true;
            }
        }
        return false;
    }

    private <T> T getWaitAutowireBean(Class<T> targetClass) {
        Objects.requireNonNull(targetClass);
        return waitAutowire.stream()
                .map(WaitAutowire::getTarget)
                .filter(targetClass::isInstance)
                .map(targetClass::cast)
                .findAny()
                .orElse(null);
    }

    public void autowire(int tryAutowireCount) {
        instanceNoArgConstructor();
        tryAutowire(tryAutowireCount);
        registers = null;
        waitAutowire = null;
    }

    private void instanceNoArgConstructor() {
        registers.removeIf(register -> {
            for (Constructor<?> constructor : register.getDeclaredConstructors()) {
                if (constructor.getParameterCount() == 0) {
                    constructor.setAccessible(true);
                    try {
                        waitAutowire.add(WaitAutowire.valueOf(constructor.newInstance()));
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        log.error(e.getMessage(), e);
                    }
                    constructor.setAccessible(false);
                    return true;
                }
            }
            return false;
        });
    }
    
    private void tryAutowire(int tryAutowireCount) {
        for (int i = 0; i < tryAutowireCount; i++) {
            tryAutowireArgsConstructor();
            tryCallInstanceCreateBean();
            tryAutowireField();
            tryAutowireMethod();
            finishAutowireMoveToBean();
            if (registers.isEmpty() && waitAutowire.isEmpty()) {
                return;
            }
        }
        registers.forEach(register -> log.error("instance failure -> {}", register.getName()));
        waitAutowire.forEach(wa -> log.error("autowire failure -> {} \n" +
                        "autowire field failure -> {} \n" +
                        "autowire method failure -> {} \n" +
                        "create bean failure -> {}",
                wa.getTarget().getClass().getName(),
                wa.getWaitAutowireFields().stream().map(Field::getName).collect(Collectors.toSet()),
                wa.getWaitAutowireMethod().stream().map(Method::getName).collect(Collectors.toSet()),
                wa.getWaitCreateBeanMethod().stream().map(Method::getName).collect(Collectors.toSet())));
        throw new IocRuntimeException("autowire failure, please avoid complex cross dependencies");
    }
    
    private void tryAutowireField() {
        waitAutowire.forEach(wa -> wa.getWaitAutowireFields().removeIf(field -> {
            Object object = getWaitAutowireBean(field.getType());
            if (object == null)
                return false;
            field.setAccessible(true);
            try {
                field.set(wa.getTarget(), object);
            } catch (IllegalAccessException e) {
                log.error(e.getMessage(), e);
            }
            field.setAccessible(false);
            return true;
        }));
    }
    
    private void tryAutowireMethod() {
        waitAutowire.forEach(wa -> wa.getWaitAutowireMethod().removeIf(method -> {
            if (method.getParameterCount() > 0) {
                for (Class<?> paramType : method.getParameterTypes()) {
                    if (getWaitAutowireBean(paramType) == null) {
                        return false;
                    }
                }
                Object[] params = new Object[method.getParameterCount()];
                for (int i = 0; i < method.getParameterCount(); i++) {
                    params[i] = getWaitAutowireBean(method.getParameterTypes()[i]);
                }
                method.setAccessible(true);
                try {
                    method.invoke(wa.getTarget(), params);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.error(e.getMessage(), e);
                }
            } else {
                method.setAccessible(true);
                try {
                    method.invoke(wa.getTarget());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.error(e.getMessage(), e);
                }
            }
            method.setAccessible(false);
            return true;
        }));
    }
    
    private void tryAutowireArgsConstructor() {
        registers.removeIf(register -> {
           for (Constructor<?> constructor : register.getDeclaredConstructors()) {
               if (!constructor.isAnnotationPresent(Autowire.class))
                   return false;
               boolean skip = false;
               for (Class<?> paramType : constructor.getParameterTypes()) {
                   if (getWaitAutowireBean(paramType) == null) {
                       skip = true;
                       break;
                   }
               }
               if (skip)
                   continue;
               Object[] params = new Object[constructor.getParameterCount()];
               for (int i = 0; i < constructor.getParameterCount(); i++) {
                   params[i] = getWaitAutowireBean(constructor.getParameterTypes()[i]);
               }
               constructor.setAccessible(true);
               try {
                   waitAutowire.add(WaitAutowire.valueOf(constructor.newInstance(params)));
               } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                   log.error(e.getMessage(), e);
               }
               constructor.setAccessible(false);
               return true;
           }
           return false;
        });
    }
    
    private void tryCallInstanceCreateBean() {
        waitAutowire.forEach(wa -> {
            if (wa.isAutowireFinish()) {
                wa.getWaitCreateBeanMethod().removeIf(method -> {
                    method.setAccessible(true);
                    try {
                        waitAutowire.add(WaitAutowire.valueOf(method.invoke(wa.getTarget())));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        log.error(e.getMessage(), e);
                    }
                    method.setAccessible(false);
                    return true;
                });
            }
        });
    }

    private void finishAutowireMoveToBean() {
        waitAutowire.removeIf(wa -> {
            if (wa.isFinish()) {
                beans.add(wa.getTarget());
                return true;
            }
            return false;
        });
    }

}
