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
            throw new RuntimeException(String.format("not found @Table, %s", entityClass.getName()));
        List<Field> fields = Lists.newLinkedList();
        collectField(fields, entityClass);
        if (fields.isEmpty())
            throw new RuntimeException(String.format("not found @Column field, %s", entityClass.getName()));
        List<Field> prePrimaryKeyFields = fields.stream()
                .filter(field -> field.isAnnotationPresent(PrimaryKey.class))
                .collect(Collectors.toList());
        if (prePrimaryKeyFields.size() > 1)
            throw new RuntimeException(String.format("@PrimaryKey field duplication, %s", entityClass.getName()));
        Field primaryKey = Iterables.getFirst(prePrimaryKeyFields, null);
        if (primaryKey == null)
            throw new RuntimeException(String.format("not found @PrimaryKey field, %s", entityClass.getName()));
        fields.remove(primaryKey);
        PrimaryKeyMeta primaryKeyMeta = parsePrimaryKey(primaryKey);
        List<ColumnMeta> columnMetas = fields.stream().map(TableMetaParser::parseColumn).collect(ImmutableList.toImmutableList());
        Set<IndexMeta> indexMetas = Arrays.stream(table.index()).map(TableMetaParser::parseIndex).collect(ImmutableSet.toImmutableSet());
        Map<String, ColumnMeta> fieldToColumnMetas = columnMetas.stream().collect(ImmutableMap.toImmutableMap(ColumnMeta::getFieldName, v -> v));
        indexMetas.stream().flatMap(indexMeta -> indexMeta.getFields().stream()).forEach(field -> {
            if (!fieldToColumnMetas.containsKey(field))
                throw new RuntimeException(String.format("not found index field[%s], %s", field, entityClass.getName()));
        });
        return TableMeta.Builder.newBuilder()
                .name(table.name().length() > 0 ? table.name() : entityClass.getSimpleName())
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
        return ColumnMeta.Builder.newBuilder()
                .name(column.name().length() > 0 ? column.name() : field.getName())
                .comment(column.comment())
                .type(column.type() != ColumnType.DEFAULT ? column.type() : getColumnType(field, column))
                .readOnly(column.readOnly())
                .length(column.length())
                .defaultValue(getDefaultValue(field))
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
                .name(index.name().length() > 0 ? index.name() : "idx_" + String.join("_", index.fields()))
                .fields(ImmutableSet.copyOf(index.fields()))
                .type(index.type())
                .build();
    }

    private static ColumnType getColumnType(Field field, Column column) {
        Class<?> type = field.getType();
        if (type == boolean.class || type == Boolean.class)
            return ColumnType.BIT;
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

    private static String getDefaultValue(Field field) {
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
