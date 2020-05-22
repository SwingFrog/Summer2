package com.swingfrog.summer2.dao.starter;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.swingfrog.summer2.dao.DataSourceTopic;

import javax.sql.DataSource;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author: toke
 */
public class SummerDataSourceTopic {

    private final ImmutableMap<String, DataSource> dataSourceMap;

    private SummerDataSourceTopic(ImmutableMap<String, DataSource> dataSourceMap) {
        this.dataSourceMap = dataSourceMap;
    }

    public void forEach(BiConsumer<String, DataSource> action) {
        dataSourceMap.forEach(action);
    }

    public static Builder newBuilder() { return new Builder(); }

    public static final class Builder {
        private Map<String, DataSource> dataSourceMap = Maps.newHashMap();

        private Builder() {}

        public Builder setDefaultDataSource(DataSource dataSource) {
            dataSourceMap.put(DataSourceTopic.DEFAULT, dataSource);
            return this;
        }

        public Builder putDataSource(String topic, DataSource dataSource) {
            dataSourceMap.put(topic, dataSource);
            return this;
        }

        public SummerDataSourceTopic build() { return new SummerDataSourceTopic(ImmutableMap.copyOf(dataSourceMap)); }
    }
}
