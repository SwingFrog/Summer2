package com.swingfrog.summer2.dao.jdbc;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author: toke
 */
public class JdbcAsyncRepositoryProcessor {

    private static final Logger log = LoggerFactory.getLogger(JdbcAsyncRepositoryProcessor.class);

    private final ScheduledExecutorService executor;
    private final Set<AbstractJdbcAsyncCacheRepository<?, ?>> repositories = Sets.newConcurrentHashSet();

    public JdbcAsyncRepositoryProcessor(int corePoolSize) {
        executor = Executors.newScheduledThreadPool(corePoolSize,
                new ThreadFactoryBuilder().setNameFormat("JdbcAsyncRepositoryProcessor-%s").build());
        log.info("jdbc async repository core pool size -> {}", corePoolSize);
    }

    public void addJdbcAsyncCacheRepository(AbstractJdbcAsyncCacheRepository<?, ?> repository) {
        if (!repositories.add(repository))
            return;
        AsyncTask asyncTask = repository.initializeAsync();
        executor.scheduleWithFixedDelay(asyncTask.getRunnable(), asyncTask.getDelayTime(), asyncTask.getDelayTime(),
                TimeUnit.MILLISECONDS);
        log.debug("add jdbc async repository -> {}", repository.getClass().getName());
    }

    public void shutdown() {
        log.debug("jdbc async repository shutdown...");
        executor.shutdown();
        try {
            while (!executor.isTerminated()) {
                executor.awaitTermination(1, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e){
            log.error(e.getMessage(), e);
        }
        repositories.forEach(AbstractJdbcAsyncCacheRepository::onShutdown);
    }

    static class AsyncTask {
        final Runnable runnable;
        final long delayTime;

        public AsyncTask(Runnable runnable, long delayTime) {
            this.runnable = runnable;
            this.delayTime = delayTime;
        }

        public Runnable getRunnable() {
            return runnable;
        }

        public long getDelayTime() {
            return delayTime;
        }
    }
}
