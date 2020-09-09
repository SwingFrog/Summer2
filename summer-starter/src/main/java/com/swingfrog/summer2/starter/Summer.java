package com.swingfrog.summer2.starter;

import com.swingfrog.summer2.starter.annotation.SummerApplication;
import com.swingfrog.summer2.starter.event.SummerContext;
import com.swingfrog.summer2.starter.event.SummerListener;
import com.swingfrog.summer2.core.configuration.ConfigurationProcessor;
import com.swingfrog.summer2.core.ioc.IocProcessor;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

/**
 * @author: toke
 */
public class Summer {

    public static void hot(Class<?> bootstrapClass) {
        hot(bootstrapClass, "summer.properties");
    }

    public static void hot(Class<?> bootstrapClass, String defaultPropertyPath) {
        hot(bootstrapClass, defaultPropertyPath, 64);
    }

    public static void hot(Class<?> bootstrapClass, String defaultPropertyPath, int tryAutowireCount) {
        SummerApplication summerApplication = bootstrapClass.getAnnotation(SummerApplication.class);
        if (summerApplication == null)
            throw new StarterRuntimeException("bootstrap class need use @SummerApplication");
        IocProcessor iocProcessor = new IocProcessor();
        Arrays.stream(summerApplication.value()).forEach(iocProcessor::scanComponent);
        new StarterProcessor(iocProcessor).foundStarter();
        iocProcessor.registerClass(bootstrapClass);
        iocProcessor.autowire(tryAutowireCount);
        new ConfigurationProcessor(iocProcessor).loadProperties(defaultPropertyPath);
        SummerContext summerContext = new SummerContext(iocProcessor);
        List<SummerListener> summerListeners = iocProcessor.listBean(SummerListener.class).stream()
                .sorted(Comparator.comparingInt(SummerListener::priority).reversed())
                .collect(Collectors.toList());
        summerListeners.forEach(summerListener -> summerListener.onPrepare(summerContext));
        summerListeners.forEach(summerListener -> summerListener.onStart(summerContext));
        Runtime.getRuntime().addShutdownHook(new Thread(()-> {
            for (ListIterator<SummerListener> iterator = summerListeners.listIterator(); iterator.hasPrevious();) {
                iterator.previous().onStop(summerContext);
            }
            for (ListIterator<SummerListener> iterator = summerListeners.listIterator(); iterator.hasPrevious();) {
                iterator.previous().onDestroy(summerContext);
            }
        }, "shutdown"));
    }

}
