package com.swingfrog.summer2.dao.jdbc;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.swingfrog.summer2.dao.meta.ColumnMeta;
import com.swingfrog.summer2.dao.meta.TableMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author: toke
 */
public class JdbcValueGenerator {

    @FunctionalInterface
    interface ThreeConsumer<T, U, O> {
        void accept(T t, U u, O o);
    }

    private static final Map<Type, ThreeConsumer<Field, Object, Long>> primaryFieldMap = Maps.newHashMap();

    static {
        ThreeConsumer<Field, Object, Long> fieldLong = (field, value, primaryValue) -> {
            try {
                field.setAccessible(true);
                field.setLong(value, primaryValue);
                field.setAccessible(false);
            } catch (IllegalAccessException e) {
                throw new JdbcRuntimeException(e);
            }
        };
        ThreeConsumer<Field, Object, Long> fieldInt = (field, value, primaryValue) -> {
            try {
                field.setAccessible(true);
                field.setInt(value, primaryValue.intValue());
                field.setAccessible(false);
            } catch (IllegalAccessException e) {
                throw new JdbcRuntimeException(e);
            }
        };
        ThreeConsumer<Field, Object, Long> fieldShort = (field, value, primaryValue) -> {
            try {
                field.setAccessible(true);
                field.setShort(value, primaryValue.shortValue());
                field.setAccessible(false);
            } catch (IllegalAccessException e) {
                throw new JdbcRuntimeException(e);
            }
        };
        ThreeConsumer<Field, Object, Long> fieldByte = (field, value, primaryValue) -> {
            try {
                field.setAccessible(true);
                field.setByte(value, primaryValue.byteValue());
                field.setAccessible(false);
            } catch (IllegalAccessException e) {
                throw new JdbcRuntimeException(e);
            }
        };
        primaryFieldMap.put(long.class, fieldLong);
        primaryFieldMap.put(Long.class, fieldLong);
        primaryFieldMap.put(int.class, fieldInt);
        primaryFieldMap.put(Integer.class, fieldInt);
        primaryFieldMap.put(short.class, fieldShort);
        primaryFieldMap.put(Short.class, fieldShort);
        primaryFieldMap.put(byte.class, fieldByte);
        primaryFieldMap.put(Byte.class, fieldByte);
    }

    public static Object convert(Object value, Type type) {
        if (isEntity(type)) {
            return JSON.toJSONString(value);
        } else {
            return value;
        }
    }

    public static Object getFieldValue(Field field, Object value) {
        Object res;
        try {
            field.setAccessible(true);
            res = field.get(value);
            field.setAccessible(false);
            res = convert(res, field.getType());
        } catch (IllegalAccessException e) {
            throw new JdbcRuntimeException(e);
        }
        return res;
    }

    public static void jsonConvertJavaBean(Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(value, JSON.parseObject(JSON.toJSONString(field.get(value)), field.getType()));
            field.setAccessible(false);
        } catch (IllegalAccessException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    public static boolean isEqualsColumnValue(ColumnMeta columnMeta, Object value, Object fieldValue) {
        Object columnValue = getColumnValue(columnMeta, value);
        if (columnValue.equals(fieldValue)) {
            return true;
        }
        return columnValue.toString().equals(fieldValue.toString());
    }

    public static Object getColumnValue(ColumnMeta columnMeta, Object value) {
        return getFieldValue(columnMeta.getField(), value);
    }

    public static Object getPrimaryKeyValue(TableMeta tableMeta, Object value) {
        return getColumnValue(tableMeta.getPrimaryKeyMeta(), value);
    }

    public static void setPrimaryKey(TableMeta tableMeta, Object value, long primaryValue) {
        Field field = tableMeta.getPrimaryKeyMeta().getField();
        Type type = field.getType();
        ThreeConsumer<Field, Object, Long> consumer = primaryFieldMap.get(type);
        if (consumer != null) {
            consumer.accept(field, value, primaryValue);
        } else {
            throw new JdbcRuntimeException("primary key must be number");
        }
    }

    public static Object[] listValueByOptional(TableMeta tableMeta, Map<String, Object> optional, Collection<String> fields) {
        return fields.stream()
                .map(field -> convert(optional.get(field), tableMeta.getFieldToColumnMetas().get(field).getField().getType()))
                .toArray();
    }

    public static Object[] listUpdateValue(TableMeta tableMeta, Object value) {
        List<Object> list = tableMeta.getColumnMetas().stream()
                .filter(columnMeta -> !columnMeta.isReadOnly())
                .map(columnMeta -> getColumnValue(columnMeta, value))
                .collect(Collectors.toList());
        list.add(getPrimaryKeyValue(tableMeta, value));
        return list.toArray();
    }

    public static Object[] listInsertValue(TableMeta tableMeta, Object value) {
        List<Object> list = Lists.newArrayListWithCapacity(tableMeta.getColumnMetas().size() + 1);
        list.add(getPrimaryKeyValue(tableMeta, value));
        tableMeta.getColumnMetas().forEach(columnMeta -> list.add(getColumnValue(columnMeta, value)));
        return list.toArray();
    }

    public static Object[] listInsertValue(TableMeta tableMeta, Object value, Object primaryKey) {
        List<Object> list = Lists.newArrayListWithCapacity(tableMeta.getColumnMetas().size() + 1);
        list.add(primaryKey);
        tableMeta.getColumnMetas().forEach(columnMeta -> list.add(getColumnValue(columnMeta, value)));
        return list.toArray();
    }

    private static final Set<Type> notEntityList = Sets.newHashSet(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            char.class, Character.class,
            String.class, Date.class);

    public static boolean isEntity(Type type) {
        return !notEntityList.contains(type);
    }

}
