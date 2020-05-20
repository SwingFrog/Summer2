package com.swingfrog.summer2.dao.jdbc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.swingfrog.summer2.dao.Repository;
import com.swingfrog.summer2.dao.jdbc.meta.JdbcColumnMeta;
import com.swingfrog.summer2.dao.jdbc.meta.JdbcIndexMeta;
import com.swingfrog.summer2.dao.jdbc.meta.JdbcTableMetaParser;
import com.swingfrog.summer2.dao.meta.ColumnMeta;
import com.swingfrog.summer2.dao.meta.IndexMeta;
import com.swingfrog.summer2.dao.meta.TableMeta;
import com.swingfrog.summer2.dao.meta.TableMetaParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
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

    private static final Logger log = LoggerFactory.getLogger(AbstractJdbcRepository.class);

    private TableMeta tableMeta;
    private AtomicLong primaryKey;

    private String insertSql;
    private String deleteSql;
    private String deleteAllSql;
    private String updateSql;
    private String selectSql;
    private String selectAllSql;
    private Map<String, String> selectOptionalSqlMap;

    @Override
    void initialize(DataSource dataSource) {
        super.initialize(dataSource);
        tableMeta = TableMetaParser.parse(getEntityClass());
        if (existsTable()) {
            updateTable();
        } else {
            createTable();
        }
        if (tableMeta.getPrimaryKeyMeta().isAutoIncrement()) {
            Object maxPk = getValue(JdbcSqlGenerator.getMaxPrimaryKey(tableMeta));
            if (maxPk == null) {
                primaryKey = new AtomicLong(incrementDefaultValue());
            } else {
                primaryKey = new AtomicLong(Long.parseLong(maxPk.toString()));
            }
        }
        insertSql = JdbcSqlGenerator.insert(tableMeta);
        deleteSql = JdbcSqlGenerator.delete(tableMeta);
        deleteAllSql = JdbcSqlGenerator.deleteAll(tableMeta);
        updateSql = JdbcSqlGenerator.update(tableMeta);
        selectSql = JdbcSqlGenerator.select(tableMeta);
        selectAllSql = JdbcSqlGenerator.selectAll(tableMeta);
        selectOptionalSqlMap = Maps.newHashMap();
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

    protected void setPrimaryKey(V value) {
        if (primaryKey == null)
            throw new JdbcRuntimeException("set primary key must be auto increment");
        JdbcValueGenerator.setPrimaryKey(tableMeta, value, primaryKey.incrementAndGet());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void add(V value) {
        if (primaryKey != null)
            setPrimaryKey(value);
        addByPrimaryKey((K) JdbcValueGenerator.getPrimaryKeyValue(tableMeta, value), value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void add(Collection<V> values) {
        if (primaryKey != null) {
            values.forEach(this::setPrimaryKey);
        }
        addByPrimaryKey(values.stream().map(value -> (K) JdbcValueGenerator.getPrimaryKeyValue(tableMeta, value)).collect(Collectors.toList()), values);
    }

    protected void addByPrimaryKey(K key, V value) {
        update(insertSql, JdbcValueGenerator.listInsertValue(tableMeta, value, key));
    }

    protected void addByPrimaryKey(Collection<K> keys, Collection<V> values) {
        Object[][] paramsList = new Object[keys.size()][];
        int i = 0;
        Iterator<K> kIterator = keys.iterator();
        Iterator<V> vIterator = values.iterator();
        while (kIterator.hasNext() && vIterator.hasNext()) {
            paramsList[i++] = JdbcValueGenerator.listInsertValue(tableMeta, vIterator.next(), kIterator.next());
        }
        batchUpdate(insertSql, paramsList);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(V value) {
        removeByPrimaryKey((K) JdbcValueGenerator.getPrimaryKeyValue(tableMeta, value));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(Collection<V> values) {
        removeByPrimaryKey(values.stream().map(value -> (K) JdbcValueGenerator.getPrimaryKeyValue(tableMeta, value)).collect(Collectors.toList()));
    }

    protected void removeByPrimaryKey(K key) {
        update(deleteSql, key);
    }

    protected void removeByPrimaryKey(Collection<K> keys) {
        Object[][] paramsList = new Object[keys.size()][];
        int i = 0;
        for (K key : keys) {
            paramsList[i++] = new Object[]{JdbcValueGenerator.getPrimaryKeyValue(tableMeta, key)};
        }
        batchUpdate(deleteSql, paramsList);
    }

    @Override
    public void removeAll() {
        update(deleteAllSql);
    }

    @Override
    public void update(V value) {
        update(updateSql, JdbcValueGenerator.listUpdateValue(tableMeta, value));
    }

    @Override
    public void update(Collection<V> values) {
        Object[][] paramsList = new Object[values.size()][];
        int i = 0;
        for (V value : values) {
            paramsList[i++] = JdbcValueGenerator.listUpdateValue(tableMeta, value);
        }
        batchUpdate(updateSql, paramsList);
    }

    @Override
    public V get(K key) {
        return get(selectSql, key);
    }

    @Override
    public V getOrCreate(K key, Supplier<V> supplier) {
        V value = get(key);
        if (value == null) {
            synchronized (getCreateLock(key)) {
                value = get(key);
                if (value == null) {
                    value = supplier.get();
                    add(value);
                }
            }
        }
        return value;
    }

    @Override
    public List<V> list(Map<String, Object> indexOptional, Predicate<V> filter) {
        List<String> fields = JdbcValueGenerator.listValidFieldByOptional(tableMeta, indexOptional);
        if (notMatchIndexField(fields)) {
            log.warn("miss index fields -> {} entity -> {}", fields, getEntityClass().getName());
        }
        return list(selectOptionalSql(fields), JdbcValueGenerator.listValidValueByOptional(tableMeta, indexOptional, fields))
                .stream()
                .filter(filter)
                .collect(Collectors.toList());
    }

    @Override
    public List<V> listAll() {
        return list(selectAllSql);
    }

    private String selectOptionalSql(List<String> fields) {
        return selectOptionalSqlMap.computeIfAbsent(String.join("-", fields).intern(),
                k -> JdbcSqlGenerator.selectOptional(tableMeta, fields));
    }

    protected boolean notMatchIndexField(List<String> indexField) {
        return tableMeta.getIndexMetas().stream().anyMatch(indexMeta -> indexMeta.getFields().containsAll(indexField));
    }

    protected TableMeta getTableMeta() {
        return tableMeta;
    }

    protected String getLock(Object ...args) {
        return Arrays.stream(args).map(Object::toString).collect(Collectors.joining("-")).trim();
    }

    protected String getCreateLock(Object key) {
        return getLock(this.getClass().getSimpleName(), tableMeta.getName(), "getOrCreate", key);
    }

}
