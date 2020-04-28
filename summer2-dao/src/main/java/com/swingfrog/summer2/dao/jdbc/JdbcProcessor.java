package com.swingfrog.summer2.dao.jdbc;

import com.google.common.collect.Maps;
import com.swingfrog.summer2.dao.DataSourceTopic;

import javax.sql.DataSource;
import java.util.Map;

/**
 * @author: toke
 */
public class JdbcProcessor {

    private final Map<String, DataSource> dataSourceMap = Maps.newHashMap();

    public void addDataSource(DataSource dataSource) {
        addDataSource(DataSourceTopic.DEFAULT, dataSource);
    }

    public void addDataSource(String topic, DataSource dataSource) {
        if (dataSource != dataSourceMap.putIfAbsent(topic, dataSource))
            throw new RuntimeException("data source topic duplicate");
    }

    public void injectDataSource(AbstractJdbcPersistent<?> abstractJdbcPersistent) {
        DataSource dataSource = dataSourceMap.get(abstractJdbcPersistent.topic());
        if (dataSource == null)
            throw new RuntimeException(String.format("data source topic[%s] not found", abstractJdbcPersistent.topic()));
        abstractJdbcPersistent.initialize(dataSource);
    }

}
