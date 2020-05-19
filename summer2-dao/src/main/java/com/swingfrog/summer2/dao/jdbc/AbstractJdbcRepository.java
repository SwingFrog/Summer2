package com.swingfrog.summer2.dao.jdbc;

import com.google.common.collect.Lists;
import com.swingfrog.summer2.dao.Repository;
import com.swingfrog.summer2.dao.jdbc.meta.JdbcColumnMeta;
import com.swingfrog.summer2.dao.jdbc.meta.JdbcIndexMeta;
import com.swingfrog.summer2.dao.jdbc.meta.JdbcTableMetaParser;
import com.swingfrog.summer2.dao.meta.ColumnMeta;
import com.swingfrog.summer2.dao.meta.IndexMeta;
import com.swingfrog.summer2.dao.meta.TableMeta;
import com.swingfrog.summer2.dao.meta.TableMetaParser;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author: toke
 */
public abstract class AbstractJdbcRepository<K, V> extends AbstractJdbcPersistent<V> implements Repository<K, V> {

    private TableMeta tableMeta;
    private AtomicLong primaryKey;

    private String insertSql;
    private String deleteSql;
    private String deleteAllSql;
    private String updateSql;
    private String selectSql;
    private Map<String, String> selectOptionSql;
    private String selectAllSql;

    @Override
    void initialize(DataSource dataSource) {
        super.initialize(dataSource);
        tableMeta = TableMetaParser.parse(getEntityClass());
        if (existsTable()) {
            updateTable();
        } else {
            createTable();
        }
    }

    private boolean existsTable() {
        return getValue(JdbcSqlGenerator.existsTable(tableMeta)) != null;
    }

    private void createTable() {
        update(JdbcSqlGenerator.createTable(tableMeta));
    }

    private void updateTable() {
        // update column
        List<Map<String, Object>> columns = listMap(JdbcSqlGenerator.listColumn(tableMeta));
        List<JdbcColumnMeta> jdbcColumnMetas = JdbcTableMetaParser.parseColumn(columns);
        List<ColumnMeta> columnMetas = Lists.newLinkedList(tableMeta.getColumnMetas());
        columnMetas.add(tableMeta.getPrimaryKeyMeta());
        List<ColumnMeta> addColumns = columnMetas.stream()
                .filter(columnMeta -> jdbcColumnMetas.stream().noneMatch(jdbcColumnMeta -> jdbcColumnMeta.isSameName(columnMeta)))
                .collect(Collectors.toList());
        List<ColumnMeta> changeColumns = columnMetas.stream()
                .filter(columnMeta -> jdbcColumnMetas.stream().anyMatch(jdbcColumnMeta -> jdbcColumnMeta.isSameName(columnMeta)))
                .filter(columnMeta -> jdbcColumnMetas.stream().noneMatch(jdbcColumnMeta -> jdbcColumnMeta.isSame(columnMeta)))
                .collect(Collectors.toList());
        addColumns.forEach(columnMeta -> update(JdbcSqlGenerator.addColumn(tableMeta, columnMeta)));
        changeColumns.forEach(columnMeta -> update(JdbcSqlGenerator.changeColumn(tableMeta, columnMeta)));

        // update index
        List<Map<String, Object>> indexes = listMap(JdbcSqlGenerator.listIndex(tableMeta));
        List<JdbcIndexMeta> jdbcIndexMetas = JdbcTableMetaParser.parseIndex(indexes);
        List<IndexMeta> addIndexes = tableMeta.getIndexMetas().stream()
                .filter(indexMeta -> !jdbcIndexMetas.removeIf(jdbcIndexMeta -> jdbcIndexMeta.isSame(indexMeta)))
                .collect(Collectors.toList());
        jdbcIndexMetas.forEach(jdbcIndexMeta -> update(JdbcSqlGenerator.removeIndex(tableMeta, jdbcIndexMeta.getName())));
        addIndexes.forEach(indexMeta -> update(JdbcSqlGenerator.addIndex(tableMeta, indexMeta)));

        // update primary key
        String primaryKeyColumn = JdbcTableMetaParser.findPrimaryKeyColumn(indexes);
        if (!primaryKeyColumn.equals(tableMeta.getPrimaryKeyMeta().getName())) {
            update(JdbcSqlGenerator.removePrimaryKey(tableMeta));
            update(JdbcSqlGenerator.addPrimaryKey(tableMeta, tableMeta.getPrimaryKeyMeta()));
        }
    }

    @Override
    protected Map<String, String> columnToPropertyOverrides() {
        Map<String, String> columnToPropertyOverrides = tableMeta.getColumnMetas().stream()
                .collect(Collectors.toMap(ColumnMeta::getFieldName, ColumnMeta::getName));
        columnToPropertyOverrides.put(tableMeta.getPrimaryKeyMeta().getFieldName(), tableMeta.getPrimaryKeyMeta().getName());
        return columnToPropertyOverrides;
    }

    protected long incrementDefaultValue() {
        return 0;
    }

    @Override
    public void add(V value) {

    }

    @Override
    public void add(Collection<V> values) {

    }

    protected void addByPrimaryKey(K key, V value) {

    }

    protected void addByPrimaryKey(Collection<K> keys, Collection<V> values) {

    }

    @Override
    public void remove(V value) {

    }

    @Override
    public void remove(Collection<V> values) {

    }

    protected void removeByPrimaryKey(K key) {

    }

    protected void removeByPrimaryKey(Collection<K> keys) {

    }

    @Override
    public void removeAll() {

    }

    @Override
    public void update(V value) {

    }

    @Override
    public void update(Collection<V> values) {

    }

    @Override
    public V get(K key) {
        return null;
    }

    @Override
    public V getOrCreate(K key, Supplier<V> supplier) {
        return null;
    }

    @Override
    public List<V> list(Map<String, Object> indexOptional, Predicate<V> filter) {
        return null;
    }

    @Override
    public List<V> listAll() {
        return null;
    }

}
