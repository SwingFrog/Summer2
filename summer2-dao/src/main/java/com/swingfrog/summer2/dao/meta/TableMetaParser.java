package com.swingfrog.summer2.dao.meta;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.swingfrog.summer2.dao.annotation.Column;
import com.swingfrog.summer2.dao.annotation.Index;
import com.swingfrog.summer2.dao.annotation.PrimaryKey;
import com.swingfrog.summer2.dao.annotation.Table;
import com.swingfrog.summer2.dao.constant.ColumnType;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author: toke
 */
public class TableMetaParser {

    public static TableMeta parse(Class<?> entityClass) {
        Table table = entityClass.getDeclaredAnnotation(Table.class);
        if (table == null)
            throw new MetaRuntimeException("not found @Table, entity -> " + entityClass.getName());
        List<Field> fields = Lists.newLinkedList();
        collectField(fields, entityClass);
        if (fields.isEmpty())
            throw new MetaRuntimeException("not found @Column field, entity -> " + entityClass.getName());
        List<Field> prePrimaryKeyFields = fields.stream()
                .filter(field -> field.isAnnotationPresent(PrimaryKey.class))
                .collect(Collectors.toList());
        if (prePrimaryKeyFields.size() > 1)
            throw new MetaRuntimeException("@PrimaryKey field duplication, entity -> " + entityClass.getName());
        Field primaryKey = Iterables.getFirst(prePrimaryKeyFields, null);
        if (primaryKey == null)
            throw new MetaRuntimeException("not found @PrimaryKey field, entity -> " + entityClass.getName());
        fields.remove(primaryKey);
        PrimaryKeyMeta primaryKeyMeta = parsePrimaryKey(primaryKey);
        List<ColumnMeta> columnMetas = fields.stream().map(TableMetaParser::parseColumn).collect(ImmutableList.toImmutableList());
        Set<IndexMeta> indexMetas = Arrays.stream(table.index()).map(TableMetaParser::parseIndex).collect(ImmutableSet.toImmutableSet());
        Map<String, ColumnMeta> fieldToColumnMetas = columnMetas.stream().collect(ImmutableMap.toImmutableMap(ColumnMeta::getFieldName, v -> v));
        indexMetas.stream().flatMap(indexMeta -> indexMeta.getFields().stream()).forEach(field -> {
            if (!fieldToColumnMetas.containsKey(field))
                throw new MetaRuntimeException("not found index field -> " + field + ", entity -> " + entityClass.getName());
        });
        return TableMeta.Builder.newBuilder()
                .name(table.name().isEmpty() ? entityClass.getSimpleName() : table.name())
                .comment(table.comment())
                .charset(table.charset())
                .collate(table.collate())
                .indexMetas(indexMetas)
                .primaryKeyMeta(primaryKeyMeta)
                .columnMetas(columnMetas)
                .fieldToColumnMetas(fieldToColumnMetas)
                .build();
    }

    private static void collectField(List<Field> list, Class<?> entityClass) {
        if (entityClass == null)
            return;
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                list.add(field);
            }
        }
        collectField(list, entityClass.getSuperclass());
    }

    private static ColumnMeta parseColumn(Field field) {
        Column column = field.getDeclaredAnnotation(Column.class);
        ColumnType columnType = column.type() == ColumnType.DEFAULT ? getColumnType(field, column) : column.type();
        return ColumnMeta.Builder.newBuilder()
                .name(column.name().isEmpty() ? field.getName() : column.name())
                .comment(column.comment())
                .type(columnType)
                .readOnly(column.readOnly())
                .length(column.length())
                .defaultValue(getDefaultValue(field, columnType))
                .field(field)
                .fieldName(field.getName())
                .build();
    }

    private static PrimaryKeyMeta parsePrimaryKey(Field field) {
        PrimaryKey primaryKey = field.getDeclaredAnnotation(PrimaryKey.class);
        return PrimaryKeyMeta.Builder.newBuilder()
                .autoIncrement(primaryKey.autoIncrement())
                .columnMeta(parseColumn(field))
                .build();
    }

    private static IndexMeta parseIndex(Index index) {
        return IndexMeta.Builder.newBuilder()
                .name(index.name().isEmpty() ? "idx_" + String.join("_", index.fields()) : index.name())
                .fields(ImmutableSet.copyOf(index.fields()))
                .type(index.type())
                .build();
    }

    private static ColumnType getColumnType(Field field, Column column) {
        Class<?> type = field.getType();
        if (type == boolean.class || type == Boolean.class)
            return ColumnType.TINYINT;
        if (type == byte.class || type == Byte.class)
            return ColumnType.TINYINT;
        if (type == short.class || type == Short.class)
            return ColumnType.SMALLINT;
        if (type == int.class || type == Integer.class)
            return ColumnType.INT;
        if (type == long.class || type == Long.class)
            return ColumnType.BIGINT;
        if (type == float.class || type == Float.class)
            return ColumnType.FLOAT;
        if (type == double.class || type == Double.class)
            return ColumnType.DOUBLE;
        if (type == Date.class || type == LocalDateTime.class)
            return ColumnType.DATETIME;
        if (type == Enum.class)
            return ColumnType.INT;
        if (type.isArray() && type.getComponentType() == Byte.class)
            return ColumnType.BLOB;
        int length = column.length();
        if (length <= 255)
            return ColumnType.CHAR;
        if (length <= 16383)
            return ColumnType.VARCHAR;
        return ColumnType.TEXT;
    }

    private static String getDefaultValue(Field field, ColumnType columnType) {
        if (columnType == ColumnType.BLOB || columnType == ColumnType.LONGBLOB)
            return null;
        if (columnType == ColumnType.TEXT || columnType == ColumnType.LONGTEXT)
            return null;
        Class<?> type = field.getType();
        if (type == boolean.class)
            return "0";
        if (type == byte.class)
            return "0";
        if (type == short.class)
            return "0";
        if (type == int.class)
            return "0";
        if (type == long.class)
            return "0";
        if (type == float.class)
            return "0";
        if (type == double.class)
            return "0";
        if (type.isArray() || Collection.class.isAssignableFrom(type))
            return "[]";
        if (Map.class.isAssignableFrom(type))
            return "{}";
        return null;
    }
}
