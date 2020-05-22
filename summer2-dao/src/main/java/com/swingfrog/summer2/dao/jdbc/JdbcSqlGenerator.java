package com.swingfrog.summer2.dao.jdbc;

import com.swingfrog.summer2.dao.constant.ColumnType;
import com.swingfrog.summer2.dao.meta.ColumnMeta;
import com.swingfrog.summer2.dao.meta.IndexMeta;
import com.swingfrog.summer2.dao.meta.PrimaryKeyMeta;
import com.swingfrog.summer2.dao.meta.TableMeta;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

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
        tableMeta.getColumnMetas().forEach(columnMeta -> builder.append(String.format(" %s,\n", createColumn(columnMeta))));
        builder.append(String.format(" PRIMARY KEY (`%s`)\n", tableMeta.getPrimaryKeyMeta().getName()));
        tableMeta.getIndexMetas().forEach(indexMeta ->  builder.append(String.format(",\n %s", createIndex(indexMeta))));
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
            builder.append(String.format("%s(11,%s) ", columnMeta.getType().name(), columnMeta.getLength()));
        } else if (columnMeta.getType() == ColumnType.DOUBLE) {
            builder.append(String.format("%s(22,%s) ", columnMeta.getType().name(), columnMeta.getLength()));
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
                String.join("`,`", indexMeta.getColumns()));
    }

    public static String addColumn(TableMeta tableMeta, ColumnMeta columnMeta) {
        return String.format("ALTER TABLE `%s` ADD COLUMN %s;", tableMeta.getName(), createColumn(columnMeta));
    }

    public static String changeColumn(TableMeta tableMeta, ColumnMeta columnMeta) {
        return String.format("ALTER TABLE `%s` MODIFY COLUMN %s;", tableMeta.getName(), createColumn(columnMeta));
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

    public static String insert(TableMeta tableMeta) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("INSERT INTO `%s`(", tableMeta.getName()));
        builder.append(String.format("`%s`,`", tableMeta.getPrimaryKeyMeta().getName()));
        builder.append(tableMeta.getColumnMetas().stream().map(ColumnMeta::getName).collect(Collectors.joining("`,`")));
        builder.append("`) VALUES(?");
        tableMeta.getColumnMetas().forEach(columnMeta -> builder.append(",?"));
        builder.append(");");
        return builder.toString();
    }

    public static String delete(TableMeta tableMeta) {
        return String.format("DELETE FROM `%s` WHERE `%s` = ?;", tableMeta.getName(), tableMeta.getPrimaryKeyMeta().getName());
    }

    public static String deleteAll(TableMeta tableMeta) {
        return String.format("DELETE FROM `%s`;", tableMeta.getName());
    }

    public static String update(TableMeta tableMeta) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("UPDATE `%s`", tableMeta.getName()));
        if (!tableMeta.getColumnMetas().isEmpty()) {
            builder.append(" SET");
            Iterator<ColumnMeta> iterator = tableMeta.getColumnMetas().iterator();
            if (iterator.hasNext()) {
                for (;;) {
                    ColumnMeta columnMeta = iterator.next();
                    if (columnMeta.isReadOnly()) {
                        if (!iterator.hasNext()) {
                            break;
                        }
                    } else{
                        builder.append(String.format(" `%s` = ?", columnMeta.getName()));
                        if (iterator.hasNext()) {
                            builder.append(",");
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        builder.append(String.format(" WHERE `%s` = ?;", tableMeta.getPrimaryKeyMeta().getName()));
        return builder.toString();
    }

    public static String select(TableMeta tableMeta) {
        return String.format("SELECT * FROM `%s` WHERE `%s` = ?;", tableMeta.getName(), tableMeta.getPrimaryKeyMeta().getName());
    }

    public static String selectOptional(TableMeta tableMeta, Collection<String> fields) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("SELECT * FROM `%s`", tableMeta.getName()));
        Iterator<String> iterator = fields.iterator();
        if (iterator.hasNext()) {
            builder.append(" WHERE");
            for (;;) {
                String field = iterator.next();
                builder.append(String.format(" `%s` = ?", tableMeta.getFieldToColumnMetas().get(field).getName()));
                if (iterator.hasNext()) {
                    builder.append(" and");
                } else {
                    break;
                }
            }
        }
        builder.append(";");
        return builder.toString();
    }

    public static String selectAll(TableMeta tableMeta) {
        return String.format("SELECT * FROM `%s`;", tableMeta.getName());
    }
}