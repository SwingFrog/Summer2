package com.swingfrog.summer2.dao.jdbc;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.swingfrog.summer2.dao.meta.IndexMeta;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author: toke
 */
public abstract class AbstractJdbcCacheRepository<K, V> extends AbstractJdbcRepository<K, V> {

    private V EMPTY;
    private Cache<K, V> cache;
    private final Map<IndexMeta, Cache<String, Set<K>>> primaryKeyCacheMap = Maps.newConcurrentMap();
    private final Map<IndexMeta, Cache<String, Boolean>> primaryKeyCacheFinishMap = Maps.newConcurrentMap();
    private final AtomicLong listAllTimestamp = new AtomicLong(0);
    long expireTime;

    // never expire if value less then zero
    protected abstract long expireTime();

    @Override
    void initialize(DataSource dataSource) {
        super.initialize(dataSource);
        expireTime = expireTime();
        try {
            EMPTY = getEntityClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new JdbcRuntimeException("cache repository EMPTY not null");
        }
        if (isNeverExpire()) {
            cache = CacheBuilder.newBuilder().build();
            getTableMeta().getIndexMetas()
                    .forEach(indexMeta -> {
                        primaryKeyCacheMap.put(indexMeta, CacheBuilder.newBuilder().build());
                        primaryKeyCacheFinishMap.put(indexMeta, CacheBuilder.newBuilder().build());
                    });
        } else {
            cache = CacheBuilder.newBuilder()
                    .expireAfterAccess(expireTime, TimeUnit.MILLISECONDS)
                    .build();
            getTableMeta().getIndexMetas()
                    .forEach(indexMeta -> {
                        primaryKeyCacheMap.put(indexMeta, CacheBuilder.newBuilder()
                                .expireAfterAccess(expireTime, TimeUnit.MILLISECONDS)
                                .build());
                        primaryKeyCacheFinishMap.put(indexMeta, CacheBuilder.newBuilder()
                                .expireAfterAccess(expireTime, TimeUnit.MILLISECONDS)
                                .build());
                    });
        }
    }

    boolean isNeverExpire() {
        return expireTime < 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void add(V value) {
        super.add(value);
        addCache((K) JdbcValueGenerator.getPrimaryKeyValue(getTableMeta(), value), value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void add(Collection<V> values) {
        super.add(values);
        values.forEach(value -> addCache((K) JdbcValueGenerator.getPrimaryKeyValue(getTableMeta(), value), value));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(V value) {
        super.remove(value);
        removeCache((K) JdbcValueGenerator.getPrimaryKeyValue(getTableMeta(), value));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(Collection<V> values) {
        super.remove(values);
        values.forEach(value -> removeCache((K) JdbcValueGenerator.getPrimaryKeyValue(getTableMeta(), value)));
    }

    @Override
    public void removeAll() {
        super.removeAll();
        removeAllCache();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void update(V value) {
        super.update(value);
        updateCache((K) JdbcValueGenerator.getPrimaryKeyValue(getTableMeta(), value), value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void update(Collection<V> values) {
        super.update(values);
        values.forEach(value -> updateCache((K) JdbcValueGenerator.getPrimaryKeyValue(getTableMeta(), value), value));
    }

    @Override
    public V get(K key) {
        V value = cache.getIfPresent(key);
        if (value == null) {
            synchronized (getCacheLock(key)) {
                value = cache.getIfPresent(key);
                if (value == null) {
                    value = super.get(key);
                    cache.put(key, value == null ? EMPTY : value);
                }
            }
        }
        if (value == EMPTY) {
            return null;
        }
        return value;
    }

    @Override
    public List<V> list(Map<String, Object> indexOptional, Predicate<V> filter) {
        if (indexOptional.isEmpty())
            return Lists.newArrayListWithCapacity(0);
        IndexMeta indexMeta = findIndexMeta(indexOptional);
        if (indexMeta == null) {
            throw new JdbcRuntimeException(String.format("miss index, fields -> %s entity -> %s",
                    indexOptional.keySet(), getEntityClass().getName()));
        }
        return listPrimaryKey(indexMeta, indexOptional).stream()
                .map(this::get)
                .filter(Objects::nonNull)
                .filter(getSafeFilter(filter))
                .filter(getNotReadOnlyFilter(indexMeta, indexOptional))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<V> listAll() {
        long time = System.currentTimeMillis();
        if (time - expireTime >= listAllTimestamp.get()) {
            synchronized (getListAllLock()) {
                if (time - expireTime >= listAllTimestamp.get()) {
                    super.listAll().forEach(value ->
                            addCache((K) JdbcValueGenerator.getPrimaryKeyValue(getTableMeta(), value), value));
                }
            }
        }
        listAllTimestamp.set(time);
        return cache.asMap().keySet().stream()
                .map(this::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    protected void addCache(K key, V value) {
        synchronized (getCacheLock(key)) {
            V old = cache.getIfPresent(key);
            if (old == null || old == EMPTY)
                cache.put(key, value);
            if (old == value || value == EMPTY)
                return;
            getTableMeta().getIndexMetas().forEach(indexMeta -> addPrimaryKeyCache(indexMeta, key, value));
        }
    }

    protected void updateCache(K key, V value) {
        synchronized (getCacheLock(key)) {
            V old = cache.getIfPresent(key);
            if (old == null || old == EMPTY) {
                cache.put(key, value);
            }
            if (value == EMPTY)
                return;
            getTableMeta().getIndexMetas().stream()
                    .filter(indexMeta -> !indexMeta.isAllReadOnly())
                    .forEach(indexMeta -> addPrimaryKeyCache(indexMeta, key, value));
        }
    }

    protected void removeCache(K key) {
        synchronized (getCacheLock(key)) {
            V old = cache.getIfPresent(key);
            cache.put(key, EMPTY);
            if (old == null || old == EMPTY)
                return;
            getTableMeta().getIndexMetas().forEach(indexMeta -> removePrimaryKeyCache(indexMeta, old));
        }
    }

    protected void removeAllCache() {
        cache.invalidateAll();
        primaryKeyCacheMap.values().forEach(Cache::invalidateAll);
        primaryKeyCacheFinishMap.values().forEach(Cache::invalidateAll);
    }

    private void addPrimaryKeyCache(IndexMeta indexMeta, K key, V value) {
        String indexFieldValue = getIndexFieldValue(indexMeta, value);
        synchronized (getIndexFieldLock(indexFieldValue)) {
            Cache<String, Set<K>> primaryKeyCache = primaryKeyCacheMap.get(indexMeta);
            Set<K> primaryKeySet = primaryKeyCache.getIfPresent(indexFieldValue);
            if (primaryKeySet == null) {
                primaryKeySet = Sets.newConcurrentHashSet();
                primaryKeyCache.put(indexFieldValue, primaryKeySet);
            }
            primaryKeySet.add(key);
        }
    }

    private void removePrimaryKeyCache(IndexMeta indexMeta, V value) {
        String indexFieldValue = getIndexFieldValue(indexMeta, value);
        synchronized (getIndexFieldLock(indexFieldValue)) {
            primaryKeyCacheMap.get(indexMeta).invalidate(indexFieldValue);
            primaryKeyCacheFinishMap.get(indexMeta).invalidate(indexFieldValue);
        }
    }

    @SuppressWarnings("unchecked")
    private Set<K> listPrimaryKey(IndexMeta indexMeta, Map<String, Object> indexOptional) {
        String indexFieldValue = getIndexFieldValue(indexMeta, indexOptional);
        synchronized (getIndexFieldLock(indexFieldValue)) {
            Cache<String, Set<K>> primaryKeyCache = primaryKeyCacheMap.get(indexMeta);
            Cache<String, Boolean> primaryKeyCacheFinish = primaryKeyCacheFinishMap.get(indexMeta);
            Set<K> primaryKeySet = primaryKeyCache.getIfPresent(indexFieldValue);
            if (primaryKeyCacheFinish.getIfPresent(indexFieldValue) == null || primaryKeySet == null) {
                primaryKeyCacheFinish.put(indexFieldValue, true);
                if (primaryKeySet == null) {
                    primaryKeySet = Sets.newConcurrentHashSet();
                    primaryKeyCache.put(indexFieldValue, primaryKeySet);
                }
                for (V value : super.list(indexOptional, null)) {
                    K key = (K) JdbcValueGenerator.getPrimaryKeyValue(getTableMeta(), value);
                    addCache(key, value);
                    primaryKeySet.add(key);
                }
            }
            return primaryKeySet;
        }
    }

    private String getIndexFieldValue(IndexMeta indexMeta, Map<String, Object> indexOptional) {
        return indexMeta.getFields()
                .stream()
                .map(indexOptional::get)
                .map(Object::toString)
                .collect(Collectors.joining("-"));
    }

    private String getIndexFieldValue(IndexMeta indexMeta, V value) {
        return indexMeta.getFields()
                .stream()
                .map(field -> getTableMeta().getFieldToColumnMetas().get(field))
                .map(columnMeta -> JdbcValueGenerator.getColumnValue(columnMeta, value))
                .map(Object::toString)
                .collect(Collectors.joining("-"));
    }

    private Predicate<V> getNotReadOnlyFilter(IndexMeta indexMeta, Map<String, Object> indexOptional) {
        if (indexMeta.isAllReadOnly())
            return getSafeFilter(null);
        return value -> indexMeta.getFields().stream()
                .map(field -> getTableMeta().getFieldToColumnMetas().get(field))
                .allMatch(columnMeta -> JdbcValueGenerator.getColumnValue(columnMeta, value).hashCode() ==
                        indexOptional.get(columnMeta.getFieldName()).hashCode());
    }

    protected String getCacheLock(Object key) {
        return getLock(this.getClass().getSimpleName(), getTableMeta().getName(), "cache", key);
    }

    protected String getIndexFieldLock(String indexFieldValue) {
        return getLock(this.getClass().getSimpleName(), getTableMeta().getName(), "indexField", indexFieldValue);
    }

    protected String getListAllLock() {
        return getLock(this.getClass().getSimpleName(), getTableMeta().getName(), "listAll");
    }

}
