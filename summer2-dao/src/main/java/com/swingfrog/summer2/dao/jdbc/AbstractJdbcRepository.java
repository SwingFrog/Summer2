package com.swingfrog.summer2.dao.jdbc;

import com.swingfrog.summer2.dao.Repository;
import com.swingfrog.summer2.dao.meta.ColumnMeta;
import com.swingfrog.summer2.dao.meta.TableMeta;
import com.swingfrog.summer2.dao.meta.TableMetaParser;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author: toke
 */
public abstract class AbstractJdbcRepository<K, V> extends AbstractJdbcPersistent<V> implements Repository<K, V> {

    private TableMeta tableMeta;
    private AtomicLong primaryKey;

    private String addSql;
    private String removeSql;
    private String removeAllSql;
    private String updateSql;
    private String getSql;
    private Map<String, String> listSqlMap;
    private String listAllSql;

    @Override
    void initialize(DataSource dataSource) {
        super.initialize(dataSource);
        tableMeta = TableMetaParser.parse(getEntityClass());
        createTable();
        updateColumn();
        updateIndex();
        updatePrimaryKey();
    }

    private void createTable() {

    }

    private void updateColumn() {

    }

    private void updateIndex() {

    }

    private void updatePrimaryKey() {

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
    public List<V> list(Map<String, Object> indexOptional, Predicate<V> filter) {
        return null;
    }

    @Override
    public List<V> listAll() {
        return null;
    }

}
