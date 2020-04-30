package com.swingfrog.summer2.dao.jdbc;

import com.swingfrog.summer2.dao.constant.ColumnType;
import com.swingfrog.summer2.dao.meta.ColumnMeta;
import com.swingfrog.summer2.dao.meta.TableMeta;

/**
 * @author: toke
 */
public class JdbcSqlGenerator {

    public static String showCreateTable(TableMeta tableMeta) {
        return String.format("SHOW CREATE TABLE `%s`;", tableMeta.getName());
    }

    public static String showFullFields(TableMeta tableMeta) {
        return String.format("SHOW FULL FIELDS FROM `%s`;", tableMeta.getName());
    }

    public static String showIndex(TableMeta tableMeta) {
        return String.format("SHOW INDEX FROM `%s`;", tableMeta.getName());
    }

    public static String selectMaxPrimaryKey(TableMeta tableMeta) {
        return String.format("SELECT MAX(`%s`) FROM `%s`;", tableMeta.getPrimaryKeyMeta().getName(), tableMeta.getName());
    }

    public static String createTable(TableMeta tableMeta) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("CREATE TABLE `%s` (\n", tableMeta.getName()));
        builder.append(" ").append(createColumn(tableMeta.getPrimaryKeyMeta(), true)).append("\n");
        for (ColumnMeta columnMeta : tableMeta.getColumnMetas()) {
            builder.append(" ").append(createColumn(columnMeta, false)).append("\n");
        }

        return builder.toString();
    }

    public static String createColumn(ColumnMeta columnMeta, boolean notNull) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("`%s` ", columnMeta.getName()));
        if (columnMeta.getType() == ColumnType.CHAR || columnMeta.getType() == ColumnType.VARCHAR) {
            builder.append(String.format("%s(%s) ", columnMeta.getType().name(), columnMeta.getLength()));
        } else {
            builder.append(String.format("%s ", columnMeta.getType().name()));
        }
        if (notNull) {
            builder.append("NOT NULL ");
        } else {
            builder.append("NULL ");
        }
        if (columnMeta.getDefaultValue() != null) {
            builder.append(String.format("DEFAULT '%s' ", columnMeta.getDefaultValue()));
        }
        builder.append(String.format("COMMENT '%s',", columnMeta.getComment()));
        return builder.toString();
    }
}