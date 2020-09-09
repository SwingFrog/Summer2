package com.swingfrog.summer2.core.configuration;

import com.swingfrog.summer2.core.configuration.annotation.Configuration;
import com.swingfrog.summer2.core.configuration.annotation.Value;
import com.swingfrog.summer2.core.ioc.IocProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

/**
 * @author: toke
 */
public class ConfigurationProcessor {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationProcessor.class);
    private static final Map<Class<?>, Function<String, Object>> convertMap = new HashMap<>();
    private static final String SPLIT = ",";
    private static final String CLASS_PATH_IDENTIFY = "classpath:";
    private final IocProcessor iocProcessor;

    public ConfigurationProcessor(IocProcessor iocProcessor) {
        this.iocProcessor = iocProcessor;
        initConvertMap();
    }

    private void initConvertMap() {
        convertMap.put(boolean.class, Boolean::valueOf);
        convertMap.put(Boolean.class, Boolean::valueOf);
        convertMap.put(byte.class, Byte::valueOf);
        convertMap.put(Byte.class, Byte::valueOf);
        convertMap.put(short.class, Short::valueOf);
        convertMap.put(Short.class, Short::valueOf);
        convertMap.put(int.class, Integer::valueOf);
        convertMap.put(Integer.class, Integer::valueOf);
        convertMap.put(long.class, Long::valueOf);
        convertMap.put(Long.class, Long::valueOf);
        convertMap.put(float.class, Float::valueOf);
        convertMap.put(Float.class, Float::valueOf);
        convertMap.put(double.class, Double::valueOf);
        convertMap.put(Double.class, Double::valueOf);
        convertMap.put(char.class, v -> v.charAt(0));
        convertMap.put(Character.class, v -> v.charAt(0));
        convertMap.put(String.class, v -> v);

        convertMap.put(boolean[].class, v -> {
            String[] values = v.split(SPLIT);
            boolean[] result = new boolean[values.length];
            for (int i = 0; i < values.length; i++) {
                result[i] = (boolean) convertMap.get(boolean.class).apply(values[i]);
            }
            return result;
        });
        convertMap.put(Boolean[].class, v -> {
            String[] values = v.split(SPLIT);
            Boolean[] result = new Boolean[values.length];
            for (int i = 0; i < values.length; i++) {
                result[i] = (Boolean) convertMap.get(Boolean.class).apply(values[i]);
            }
            return result;
        });
        convertMap.put(byte[].class, v -> {
            String[] values = v.split(SPLIT);
            byte[] result = new byte[values.length];
            for (int i = 0; i < values.length; i++) {
                result[i] = (byte) convertMap.get(byte.class).apply(values[i]);
            }
            return result;
        });
        convertMap.put(Byte[].class, v -> {
            String[] values = v.split(SPLIT);
            Byte[] result = new Byte[values.length];
            for (int i = 0; i < values.length; i++) {
                result[i] = (Byte) convertMap.get(Byte.class).apply(values[i]);
            }
            return result;
        });
        convertMap.put(short[].class, v -> {
            String[] values = v.split(SPLIT);
            short[] result = new short[values.length];
            for (int i = 0; i < values.length; i++) {
                result[i] = (short) convertMap.get(short.class).apply(values[i]);
            }
            return result;
        });
        convertMap.put(Short[].class, v -> {
            String[] values = v.split(SPLIT);
            Short[] result = new Short[values.length];
            for (int i = 0; i < values.length; i++) {
                result[i] = (Short) convertMap.get(Short.class).apply(values[i]);
            }
            return result;
        });
        convertMap.put(int[].class, v -> {
            String[] values = v.split(SPLIT);
            int[] result = new int[values.length];
            for (int i = 0; i < values.length; i++) {
                result[i] = (int) convertMap.get(int.class).apply(values[i]);
            }
            return result;
        });
        convertMap.put(Integer[].class, v -> {
            String[] values = v.split(SPLIT);
            Integer[] result = new Integer[values.length];
            for (int i = 0; i < values.length; i++) {
                result[i] = (Integer) convertMap.get(Integer.class).apply(values[i]);
            }
            return result;
        });
        convertMap.put(long[].class, v -> {
            String[] values = v.split(SPLIT);
            long[] result = new long[values.length];
            for (int i = 0; i < values.length; i++) {
                result[i] = (long) convertMap.get(long.class).apply(values[i]);
            }
            return result;
        });
        convertMap.put(Long[].class, v -> {
            String[] values = v.split(SPLIT);
            Long[] result = new Long[values.length];
            for (int i = 0; i < values.length; i++) {
                result[i] = (Long) convertMap.get(Long.class).apply(values[i]);
            }
            return result;
        });
        convertMap.put(float[].class, v -> {
            String[] values = v.split(SPLIT);
            float[] result = new float[values.length];
            for (int i = 0; i < values.length; i++) {
                result[i] = (float) convertMap.get(float.class).apply(values[i]);
            }
            return result;
        });
        convertMap.put(Float[].class, v -> {
            String[] values = v.split(SPLIT);
            Float[] result = new Float[values.length];
            for (int i = 0; i < values.length; i++) {
                result[i] = (Float) convertMap.get(Float.class).apply(values[i]);
            }
            return result;
        });
        convertMap.put(double[].class, v -> {
            String[] values = v.split(SPLIT);
            double[] result = new double[values.length];
            for (int i = 0; i < values.length; i++) {
                result[i] = (double) convertMap.get(double.class).apply(values[i]);
            }
            return result;
        });
        convertMap.put(Double[].class, v -> {
            String[] values = v.split(SPLIT);
            Double[] result = new Double[values.length];
            for (int i = 0; i < values.length; i++) {
                result[i] = (Double) convertMap.get(Double.class).apply(values[i]);
            }
            return result;
        });
        convertMap.put(char[].class, v -> {
            String[] values = v.split(SPLIT);
            char[] result = new char[values.length];
            for (int i = 0; i < values.length; i++) {
                result[i] = (char) convertMap.get(char.class).apply(values[i]);
            }
            return result;
        });
        convertMap.put(Character[].class, v -> {
            String[] values = v.split(SPLIT);
            Character[] result = new Character[values.length];
            for (int i = 0; i < values.length; i++) {
                result[i] = (Character) convertMap.get(Character.class).apply(values[i]);
            }
            return result;
        });
        convertMap.put(String[].class, v -> v.split(SPLIT));
    }

    public void loadProperties(String defaultPropertyPath) {
        List<Object> configurations = iocProcessor.listBeanByAnnotation(Configuration.class);
        Map<String, Properties> propertiesMap = new HashMap<>(configurations.size());
        List<InputStream> inputStreams = new ArrayList<>(configurations.size());
        try {
            for (Object configuration : configurations) {
                final Class<?> targetClass = configuration.getClass();
                Configuration annotation = targetClass.getDeclaredAnnotation(Configuration.class);
                if (annotation == null)
                    throw new ConfigurationRuntimeException("configuration class need use @Configuration -> " + targetClass.getName());
                Map<String, Field> valueMap = findFieldValue(targetClass);
                if (valueMap.isEmpty())
                    continue;
                String propertyPath = annotation.value().isEmpty() ? defaultPropertyPath : annotation.value();
                Properties properties = propertiesMap.computeIfAbsent(propertyPath, k -> {
                    InputStream inputStream = getPropertyInputStream(propertyPath);
                    if (inputStream == null)
                        throw new ConfigurationRuntimeException("can not found configuration properties file -> " + propertyPath);
                    inputStreams.add(inputStream);
                    Properties tempProperties = new Properties();
                    try {
                        tempProperties.load(inputStream);
                    } catch (IOException e) {
                        throw new ConfigurationRuntimeException(e);
                    }
                    return tempProperties;
                });
                autowireValue(properties, configuration, valueMap);
            }
        } finally {
            inputStreams.forEach(inputStream -> {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            });
        }
    }

    private Map<String, Field> findFieldValue(Class<?> targetClass) {
        Map<String, Field> valueMap = new HashMap<>();
        Class<?> tempClass = targetClass;
        while (tempClass != null) {
            for (Field field : targetClass.getDeclaredFields()) {
                Value value = field.getDeclaredAnnotation(Value.class);
                if (value != null) {
                    valueMap.put(value.value(), field);
                }
            }
            tempClass = tempClass.getSuperclass();
        }
        return valueMap;
    }

    private InputStream getPropertyInputStream(String path) {
        if (path.startsWith(CLASS_PATH_IDENTIFY)) {
            path = path.substring(CLASS_PATH_IDENTIFY.length());
            return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        }
        File propertyFile = new File(path);
        if (propertyFile.exists() && propertyFile.isFile()) {
            try {
                return new FileInputStream(propertyFile);
            } catch (FileNotFoundException ignored) {

            }
        }
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    }

    private void autowireValue(Properties properties, Object object, Map<String, Field> valueMap) {
        valueMap.forEach((key, field) -> {
            if (properties.containsKey(key)) {
                if (!convertMap.containsKey(field.getType()))
                    throw new ConfigurationRuntimeException("can not convert type -> " + field.getType().getName());
                String propertyValue = properties.getProperty(key);
                if (propertyValue.isEmpty())
                    return;
                Object result = convertMap.get(field.getType()).apply(propertyValue);
                field.setAccessible(true);
                try {
                    field.set(object, result);
                } catch (IllegalAccessException e) {
                    log.error(e.getMessage(), e);
                }
                field.setAccessible(false);
            }
        });
    }

}
