package com.swingfrog.summer2.dao.jdbc;

import com.swingfrog.summer2.dao.constant.ColumnType;
import com.swingfrog.summer2.dao.meta.ColumnMeta;
import com.swingfrog.summer2.dao.meta.IndexMeta;
import com.swingfrog.summer2.dao.meta.PrimaryKeyMeta;
import com.swingfrog.summer2.dao.meta.TableMeta;

/**
 * @author: toke
 */
public class JdbcSqlGenerator {

    public static String existsTable(TableMeta tableMeta) {
        return String.format("SHOW TABLES LIKE '%s';", tableMeta.getName());
    }

    public static String listColumn(TableMeta tableMeta) {
        return String.format("SHOW FULL FIELDS FROM `%s`;", tableMeta.getName());
    }

    public static String listIndex(TableMeta tableMeta) {
        return String.format("SHOW INDEX FROM `%s`;", tableMeta.getName());
    }

    public static String getMaxPrimaryKey(TableMeta tableMeta) {
        return String.format("SELECT MAX(`%s`) FROM `%s`;", tableMeta.getPrimaryKeyMeta().getName(), tableMeta.getName());
    }

    public static String createTable(TableMeta tableMeta) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("CREATE TABLE `%s` (\n", tableMeta.getName()));
        builder.append(String.format(" %s,\n", createColumn(tableMeta.getPrimaryKeyMeta())));
        for (ColumnMeta columnMeta : tableMeta.getColumnMetas()) {
            builder.append(String.format(" %s,\n", createColumn(columnMeta)));
        }
        builder.append(String.format(" PRIMARY KEY (`%s`)\n", tableMeta.getPrimaryKeyMeta().getName()));
        for (IndexMeta indexMeta :tableMeta.getIndexMetas()) {
            builder.append(String.format(",\n %s", createIndex(indexMeta)));
        }
        builder.append(String.format("\n) DEFAULT CHARACTER SET = %s COLLATE = %s COMMENT = '%s';",
                tableMeta.getCharset(), tableMeta.getCollate(), tableMeta.getComment()));
        return builder.toString();
    }

    public static String createColumn(ColumnMeta columnMeta) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("`%s` ", columnMeta.getName()));
        if (columnMeta.getType() == ColumnType.CHAR || columnMeta.getType() == ColumnType.VARCHAR) {
            builder.append(String.format("%s(%s) ", columnMeta.getType().name(), columnMeta.getLength()));
        } else if (columnMeta.getType() == ColumnType.FLOAT) {
            builder.append(String.format("%s(11,%s) ", columnMeta.getType().name(), columnMeta.getLength() > 11 ? 2 : columnMeta.getLength()));
        } else if (columnMeta.getType() == ColumnType.DOUBLE) {
            builder.append(String.format("%s(22,%s) ", columnMeta.getType().name(), columnMeta.getLength() > 11 ? 2 : columnMeta.getLength()));
        } else {
            builder.append(String.format("%s ", columnMeta.getType().name()));
        }
        if (columnMeta.isNotNull()) {
            builder.append("NOT NULL ");
        } else {
            builder.append("NULL ");
        }
        if (columnMeta.getDefaultValue() != null) {
            builder.append(String.format("DEFAULT '%s' ", columnMeta.getDefaultValue()));
        }
        builder.append(String.format("COMMENT '%s'", columnMeta.getComment()));
        return builder.toString();
    }

    public static String createIndex(IndexMeta indexMeta) {
        return String.format("%s INDEX `%s` (`%s`)",
                indexMeta.getType().name(),
                indexMeta.getName(),
                String.join("`,`", indexMeta.getFields()));
    }

    public static String addColumn(TableMeta tableMeta, ColumnMeta columnMeta) {
        return String.format("ALTER TABLE `%s` ADD COLUMN %s;", tableMeta.getName(), createColumn(columnMeta));
    }

    public static String changeColumn(TableMeta tableMeta, String originalColumnName, ColumnMeta columnMeta) {
        return String.format("ALTER TABLE `%s` CHANGE COLUMN `%s` %s;", tableMeta.getName(), originalColumnName, createColumn(columnMeta));
    }

    public static String addIndex(TableMeta tableMeta, IndexMeta indexMeta) {
        return String.format("ALTER TABLE `%s` ADD %s;", tableMeta.getName(), createIndex(indexMeta));
    }

    public static String removeIndex(TableMeta tableMeta, String indexName) {
        return String.format("ALTER TABLE `%s` DROP INDEX `%s`;", tableMeta.getName(), indexName);
    }

    public static String addPrimaryKey(TableMeta tableMeta, PrimaryKeyMeta primaryKeyMeta) {
        return String.format("ALTER TABLE `%s` ADD PRIMARY KEY (`%s`);", tableMeta.getName(), primaryKeyMeta.getName());
    }

    public static String removePrimaryKey(TableMeta tableMeta) {
        return String.format("ALTER TABLE `%s` DROP PRIMARY KEY;", tableMeta.getName());
    }

}