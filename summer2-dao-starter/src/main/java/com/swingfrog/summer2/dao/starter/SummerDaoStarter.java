package com.swingfrog.summer2.dao.starter;

import com.swingfrog.summer2.core.ioc.annotation.Autowire;
import com.swingfrog.summer2.core.ioc.annotation.Component;
import com.swingfrog.summer2.dao.jdbc.AbstractJdbcAsyncCacheRepository;
import com.swingfrog.summer2.dao.jdbc.AbstractJdbcPersistent;
import com.swingfrog.summer2.dao.jdbc.JdbcAsyncRepositoryProcessor;
import com.swingfrog.summer2.dao.jdbc.JdbcProcessor;
import com.swingfrog.summer2.starter.event.SummerContext;
import com.swingfrog.summer2.starter.event.SummerListener;

/**
 * @author: toke
 */
@Component
public class SummerDaoStarter implements SummerListener {

    private JdbcAsyncRepositoryProcessor jdbcAsyncRepositoryProcessor;

    @Autowire
    private SummerDaoConfiguration summerDaoConfiguration;

    @Autowire
    private SummerDataSourceTopic summerDataSourceTopic;

    @Override
    public int priority() {
        Integer priority = summerDaoConfiguration.getDaoStarterPriority();
        if (priority != null)
            return priority;
        return SummerListener.PRIORITY_SYSTEM;
    }

    @Override
    public void onPrepare(SummerContext context) {
        JdbcProcessor jdbcProcessor = new JdbcProcessor();
        summerDataSourceTopic.forEach(jdbcProcessor::addDataSource);
        context.listBean(AbstractJdbcPersistent.class).forEach(jdbcProcessor::injectDataSource);
        jdbcAsyncRepositoryProcessor = new JdbcAsyncRepositoryProcessor(summerDaoConfiguration.getDaoAsyncCorePoolSize());
        context.listBean(AbstractJdbcAsyncCacheRepository.class).forEach(jdbcAsyncRepositoryProcessor::addJdbcAsyncCacheRepository);
    }

    @Override
    public void onStart(SummerContext context) {

    }

    @Override
    public void onStop(SummerContext context) {

    }

    @Override
    public void onDestroy(SummerContext context) {
        jdbcAsyncRepositoryProcessor.shutdown();
    }

}
