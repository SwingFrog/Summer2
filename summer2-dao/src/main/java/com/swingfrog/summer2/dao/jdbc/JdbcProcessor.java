package com.swingfrog.summer2.dao.jdbc;

import com.google.common.collect.Maps;
import com.swingfrog.summer2.dao.DataSourceTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Map;

/**
 * @author: toke
 */
public class JdbcProcessor {

    private static final Logger log = LoggerFactory.getLogger(JdbcProcessor.class);

    private final Map<String, DataSource> dataSourceMap = Maps.newHashMap();

    public void addDataSource(DataSource dataSource) {
        addDataSource(DataSourceTopic.DEFAULT, dataSource);
    }

    public void addDataSource(String topic, DataSource dataSource) {
        if (dataSourceMap.putIfAbsent(topic, dataSource) != null)
            throw new JdbcRuntimeException("data source topic duplicate -> " + topic);
        log.info("add data source topic -> {}", topic);
    }

    public void injectDataSource(AbstractJdbcPersistent<?> abstractJdbcPersistent) {
        DataSource dataSource = dataSourceMap.get(abstractJdbcPersistent.topic());
        if (dataSource == null)
            throw new JdbcRuntimeException("data source topic not found -> " + abstractJdbcPersistent.topic());
        abstractJdbcPersistent.initialize(dataSource);
        log.debug("inject data source for jdbc persistent -> {}", abstractJdbcPersistent.getClass().getName());
    }

}
