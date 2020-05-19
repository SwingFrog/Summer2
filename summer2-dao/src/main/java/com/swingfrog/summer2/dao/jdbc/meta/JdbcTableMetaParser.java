package com.swingfrog.summer2.dao.jdbc.meta;

import com.swingfrog.summer2.dao.constant.ColumnType;
import com.swingfrog.summer2.dao.constant.IndexType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author: toke
 */
public class JdbcTableMetaParser {

    private static final String FIELD = "Field";
    private static final String TYPE = "Type";
    private static final String NULL = "Null";
    private static final String DEFAULT = "Default";
    private static final String COMMENT = "Comment";
    private static final String NO = "NO";

    private static final String PRIMARY = "PRIMARY";
    private static final String NON_UNIQUE = "Non_unique";
    private static final String KEY_NAME = "Key_name";
    private static final String COLUMN_NAME = "Column_name";
    private static final String INDEX_TYPE = "Index_type";

    public static List<JdbcColumnMeta> parseColumn(List<Map<String, Object>> columns) {
        return columns.stream().map(JdbcTableMetaParser::parseColumn).collect(Collectors.toList());
    }

    public static JdbcColumnMeta parseColumn(Map<String, Object> column) {
        return JdbcColumnMeta.Builder.newBuilder()
                .name(column.get(FIELD).toString())
                .comment(column.get(COMMENT).toString())
                .type(getColumnType(column.get(TYPE).toString()))
                .length(getColumnLength(column.get(TYPE).toString()))
                .defaultValue(column.get(DEFAULT).toString())
                .notNull(NO.equals(column.get(NULL).toString()))
                .build();
    }

    private static ColumnType getColumnType(String typeAndLength) {
        int index = typeAndLength.indexOf("(");
        if (index == -1) {
            return ColumnType.valueOf(typeAndLength.toUpperCase());
        }
        String type = typeAndLength.substring(0, index);
        return ColumnType.valueOf(type.toUpperCase());
    }

    private static int getColumnLength(String typeAndLength) {
        int startIndex = typeAndLength.indexOf("(");
        if (startIndex == -1) {
            return 0;
        }
        int endIndex = typeAndLength.lastIndexOf(")");
        String length = typeAndLength.substring(startIndex + 1, endIndex);
        int index = length.indexOf(",");
        if (index > -1) {
            length = length.substring(index + 1);
        }
        return Integer.parseInt(length);
    }

    public static List<JdbcIndexMeta> parseIndex(List<Map<String, Object>> indexes) {
        Map<String, List<Map<String, Object>>> map = indexes.stream().collect(Collectors.groupingBy(index -> index.get(KEY_NAME).toString()));
        map.remove(PRIMARY);
        return map.values().stream()
                .map(v -> parseIndex(Objects.requireNonNull(v.stream().findAny().orElse(null)),
                        v.stream().map(index -> index.get(COLUMN_NAME).toString())
                                .collect(Collectors.toSet()))).collect(Collectors.toList());
    }

    public static JdbcIndexMeta parseIndex(Map<String, Object> index, Set<String> columns) {
        return JdbcIndexMeta.Builder.newBuilder()
                .name(index.get(KEY_NAME).toString())
                .columns(columns)
                .type(getIndexType(index))
                .build();
    }

    private static IndexType getIndexType(Map<String, Object> index) {
        if ("0".equals(index.get(NON_UNIQUE)))
            return IndexType.UNIQUE;
        if (IndexType.FULLTEXT.name().equals(index.get(INDEX_TYPE)))
            return IndexType.FULLTEXT;
        return IndexType.NORMAL;
    }

    public static String findPrimaryKeyColumn(List<Map<String, Object>> indexes) {
        return indexes.stream()
                .filter(index -> PRIMARY.equals(index.get(KEY_NAME)))
                .map(index -> index.get(COLUMN_NAME).toString())
                .findFirst()
                .orElse(null);
    }
}
