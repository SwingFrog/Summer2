package com.swingfrog.summer2.dao.jdbc;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

/**
 * @author: toke
 */
public abstract class AbstractJdbcCacheRepository<K, V> extends AbstractJdbcRepository<K, V> {

    private V EMPTY;
    private final Cache<K, V> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(expireTime(), TimeUnit.MILLISECONDS)
            .build();
    private final Map<String, Cache<Object, Set<K>>> cachePrimaryKeyMap = Maps.newConcurrentMap();
    private final Map<String, Cache<Object, Boolean>> cachePrimaryKeyFinishMap = Maps.newConcurrentMap();
    private final AtomicLong listAllTimestamp = new AtomicLong(0);
    private final long expireTime = expireTime();

    @Override
    void initialize(DataSource dataSource) {
        super.initialize(dataSource);
        try {
            EMPTY = getEntityClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new JdbcRuntimeException("cache repository EMPTY not null");
        }
        getTableMeta().getIndexMetas().stream()
                .flatMap(indexMeta -> indexMeta.getFields().stream())
                .distinct()
                .forEach(field -> {
                    cachePrimaryKeyMap.put(field, CacheBuilder.newBuilder().expireAfterAccess(expireTime, TimeUnit.MILLISECONDS).build());
                    cachePrimaryKeyFinishMap.put(field, CacheBuilder.newBuilder().expireAfterAccess(expireTime, TimeUnit.MILLISECONDS).build());
                });
    }

    protected abstract long expireTime();

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
        return null;
    }

    @Override
    public List<V> listAll() {
        return null;
    }

    protected void addCache(K key, V value) {

    }

    protected void removeCache(K key) {

    }

    protected void removeAllCache() {

    }

    protected String getCacheLock(Object key) {
        return getLock(this.getClass().getSimpleName(), getTableMeta().getName(), "getCache", key);
    }

}
