package com.swingfrog.summer2.dao.jdbc;

import com.google.common.collect.ImmutableMap;
import com.swingfrog.summer2.dao.DataSourceTopic;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author: toke
 */
public abstract class AbstractJdbcPersistent<T> {

    private static final Logger log = LoggerFactory.getLogger(AbstractJdbcPersistent.class);

    private Class<T> entityClass;
    private DataSource dataSource;
    private QueryRunner queryRunner = new QueryRunner();
    private BeanHandler<T> beanHandler;
    private BeanListHandler<T> beanListHandler;

    @SuppressWarnings("unchecked")
    void initialize(DataSource dataSource) {
        Objects.requireNonNull(dataSource);
        this.dataSource = dataSource;
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) superClass;
            Type[] typeArgs = parameterizedType.getActualTypeArguments();
            if (typeArgs != null && typeArgs.length > 0) {
                if (typeArgs[0] instanceof Class) {
                    entityClass = (Class<T>) typeArgs[0];
                }
            }
        }
        if (entityClass == null)
            throw new RuntimeException(String.format("persistent initialize failure, %s", this.getClass().getName()));
        Map<String, String> columnToPropertyOverrides = columnToPropertyOverrides();
        beanHandler = new BeanHandler<>(entityClass, new BasicRowProcessor(new PersistentBeanProcessor(columnToPropertyOverrides)));
        beanListHandler = new BeanListHandler<>(entityClass, new BasicRowProcessor(new PersistentBeanProcessor(columnToPropertyOverrides)));
    }

    protected Map<String, String> columnToPropertyOverrides() {
        return ImmutableMap.of();
    }

    protected String topic() {
        return DataSourceTopic.DEFAULT;
    }

    protected Class<T> getEntityClass() {
        return entityClass;
    }

    protected void update(String sql, Object... params) {
        Connection connection = getConnection();
        try {
            queryRunner.update(connection, sql, params);
        } catch (Exception e) {
            throw new RuntimeException(String.format("persistent update failure, %s", this.getClass().getName()));
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
            showSql(sql, params);
        }
    }

    protected void batchUpdate(String sql, Object[][] paramsList) {
        Connection connection = getConnection();
        try {
            queryRunner.batch(connection, sql, paramsList);
        } catch (Exception e) {
            throw new RuntimeException(String.format("persistent batch update failure, %s", this.getClass().getName()));
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
            if (log.isDebugEnabled()) {
                for (Object[] params : paramsList) {
                    log.debug("{} {}", sql, params);
                }
            }
        }
    }

    protected T get(String sql, Object... params) {
        Connection connection = getConnection();
        try {
            return queryRunner.query(connection, sql, beanHandler, params);
        } catch (Exception e) {
            throw new RuntimeException(String.format("persistent get failure, %s", this.getClass().getName()));
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
            showSql(sql, params);
        }
    }

    protected List<T> list(String sql, Object... params) {
        Connection connection = getConnection();
        try {
            return queryRunner.query(connection, sql, beanListHandler, params);
        } catch (Exception e) {
            throw new RuntimeException(String.format("persistent list failure, %s", this.getClass().getName()));
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
            showSql(sql, params);
        }
    }

    protected <V> V getValue(String sql, Object... params) {
        Connection connection = getConnection();
        try {
            return queryRunner.query(connection, sql, new ScalarHandler<>(), params);
        } catch (Exception e) {
            throw new RuntimeException(String.format("persistent get value failure, %s", this.getClass().getName()));
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
            showSql(sql, params);
        }
    }

    protected <V> List<V> listValue(String sql, Object... params) {
        Connection connection = getConnection();
        try {
            return queryRunner.query(connection, sql, new ColumnListHandler<>(), params);
        } catch (Exception e) {
            throw new RuntimeException(String.format("persistent list value failure, %s", this.getClass().getName()));
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
            showSql(sql, params);
        }
    }

    private Connection getConnection() {
        if (dataSource == null)
            throw new RuntimeException(String.format("persistent not execute initialize method, %s", this.getClass().getName()));
        try {
            return dataSource.getConnection();
        } catch (Exception e) {
            throw new RuntimeException(String.format("persistent get connection failure, %s", this.getClass().getName()));
        }
    }

    private void showSql(String sql, Object... params) {
        if (log.isDebugEnabled())
            log.debug("{} {}", sql, params);
    }

}
