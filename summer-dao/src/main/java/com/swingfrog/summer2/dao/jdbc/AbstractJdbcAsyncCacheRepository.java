package com.swingfrog.summer2.dao.jdbc;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author: toke
 */
public abstract class AbstractJdbcAsyncCacheRepository<K, V> extends AbstractJdbcCacheRepository<K, V> {

    private static final Logger log = LoggerFactory.getLogger(AbstractJdbcAsyncCacheRepository.class);

    private final Set<K> waitAdd = Sets.newConcurrentHashSet();
    private final Queue<Change<K, V>> waitChange = Queues.newConcurrentLinkedQueue();
    private final Map<K, Update<K, V>> waitUpdate = Maps.newConcurrentMap();
    long delayTime;

    protected abstract long delayTime();

    @Override
    void initialize(DataSource dataSource) {
        super.initialize(dataSource);
        delayTime = delayTime();
        if (!isNeverExpire() && delayTime >= expireTime) {
            throw new JdbcRuntimeException(String.format("async cache repository delayTime[%s] must be less than expireTime[%s], entity -> %s",
                    delayTime, expireTime, getEntityClass().getName()));
        }
    }

    JdbcAsyncRepositoryProcessor.AsyncTask initializeAsync() {
        return new JdbcAsyncRepositoryProcessor.AsyncTask(() -> delayExecute(false), delayTime);
    }

    void onShutdown() {
        delayExecute(true);
    }

    private void delayExecute(boolean force) {
        try {
            while (!waitChange.isEmpty()) {
                Change<K, V> change = waitChange.poll();
                switch (change.flag) {
                    case ADD:
                        if (waitAdd.remove(change.primaryKey))
                            super.addByPrimaryKey(change.primaryKey, change.value);
                        break;
                    case REMOVE:
                        super.removeByPrimaryKey(change.primaryKey);
                        break;
                    case REMOVE_ALL:
                        super.removeAll();
                        break;
                }
            }
            long time = System.currentTimeMillis();
            List<Update<K, V>> list = waitUpdate.values().stream()
                    .filter(update -> force || time - update.time >= delayTime)
                    .collect(Collectors.toList());
            if (!list.isEmpty())
                super.update(list.stream().map(value -> value.value).collect(Collectors.toList()));
            list.stream().filter(update -> force || time - update.time >= delayTime).forEach(update -> waitUpdate.remove(update.primaryKey));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void add(V value) {
        setPrimaryKey(value);
        K primaryKey = (K) JdbcValueGenerator.getPrimaryKeyValue(getTableMeta(), value);
        addCache(primaryKey, value);
        waitAdd.add(primaryKey);
        waitChange.add(Change.ofAdd(primaryKey, value));
    }

    @Override
    public void add(Collection<V> values) {
        values.forEach(this::add);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(V value) {
        K primaryKey = (K) JdbcValueGenerator.getPrimaryKeyValue(getTableMeta(), value);
        removeCache(primaryKey);
        if (waitAdd.remove(primaryKey))
            return;
        waitChange.add(Change.ofRemove(primaryKey));
    }

    @Override
    public void remove(Collection<V> values) {
        values.forEach(this::remove);
    }

    @Override
    public void removeAll() {
        waitAdd.clear();
        waitChange.clear();
        waitUpdate.clear();
        removeAllCache();
        waitChange.add(Change.ofRemoveAll());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void update(V value) {
        K primaryKey = (K) JdbcValueGenerator.getPrimaryKeyValue(getTableMeta(), value);
        if (get(primaryKey) == null)
            throw new JdbcRuntimeException(String.format("can't update primary key -> %s, entity -> %s", primaryKey, getEntityClass().getName()));
        updateCache(primaryKey, value);
        waitUpdate.computeIfAbsent(primaryKey, k -> Update.of(primaryKey, value, System.currentTimeMillis()));
    }

    @Override
    public void update(Collection<V> values) {
        values.forEach(this::update);
    }

    private enum ChangeFlag {
        ADD, REMOVE, REMOVE_ALL
    }

    private static class Change<K, V> {
        final K primaryKey;
        final V value;
        final ChangeFlag flag;

        private Change(K primaryKey, V value, ChangeFlag flag) {
            this.primaryKey = primaryKey;
            this.value = value;
            this.flag = flag;
        }

        public static <K, V> Change<K, V> ofAdd(K primaryKey, V value) {
            return new Change<>(primaryKey, value, ChangeFlag.ADD);
        }

        public static <K, V> Change<K, V> ofRemove(K primaryKey) {
            return new Change<>(primaryKey, null, ChangeFlag.REMOVE);
        }

        public static <K, V> Change<K, V> ofRemoveAll() {
            return new Change<>(null, null, ChangeFlag.REMOVE_ALL);
        }
    }

    private static class Update<K, V> {
        final K primaryKey;
        final V value;
        final long time;

        private Update(K primaryKey, V value, long time) {
            this.primaryKey = primaryKey;
            this.value = value;
            this.time = time;
        }

        public static <K, V> Update<K, V> of(K primaryKey, V value, long time) {
            return new Update<>(primaryKey, value, time);
        }

    }

}
