package com.swingfrog.summer2.dao.jdbc;

import com.google.common.collect.ImmutableMap;
import com.swingfrog.summer2.dao.DataSourceTopic;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
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

    void initialize(DataSource dataSource) {
        Objects.requireNonNull(dataSource);
        this.dataSource = dataSource;
        Map<String, String> columnToPropertyOverrides = columnToPropertyOverrides();
        beanHandler = new BeanHandler<>(getEntityClass(), new BasicRowProcessor(new PersistentBeanProcessor(columnToPropertyOverrides)));
        beanListHandler = new BeanListHandler<>(getEntityClass(), new BasicRowProcessor(new PersistentBeanProcessor(columnToPropertyOverrides)));
    }

    protected Map<String, String> columnToPropertyOverrides() {
        return ImmutableMap.of();
    }

    protected String topic() {
        return DataSourceTopic.DEFAULT;
    }

    @SuppressWarnings("unchecked")
    protected Class<T> getEntityClass() {
        if (entityClass == null) {
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
                throw new JdbcRuntimeException("persistent initialize failure, entity -> " + this.getClass().getName());
        }
        return entityClass;
    }

    protected void update(String sql, Object... params) {
        Connection connection = getConnection();
        try {
            queryRunner.update(connection, sql, params);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new JdbcRuntimeException("persistent update failure, entity -> " + this.getClass().getName());
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
            log.error(e.getMessage(), e);
            throw new JdbcRuntimeException("persistent batch update failure, entity -> " + this.getClass().getName());
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
            log.error(e.getMessage(), e);
            throw new JdbcRuntimeException("persistent get failure, entity -> " + this.getClass().getName());
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
            log.error(e.getMessage(), e);
            throw new JdbcRuntimeException("persistent list failure, entity -> " + this.getClass().getName());
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
            showSql(sql, params);
        }
    }

    protected <V> V getEntity(String sql, Class<V> entityClass, Object... params) {
        Connection connection = getConnection();
        try {
            return queryRunner.query(connection, sql, new BeanHandler<>(entityClass), params);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new JdbcRuntimeException("persistent get by class failure, entity -> " + entityClass.getName());
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
            showSql(sql, params);
        }
    }

    protected <V> List<V> listEntity(String sql, Class<V> entityClass, Object... params) {
        Connection connection = getConnection();
        try {
            return queryRunner.query(connection, sql, new BeanListHandler<>(entityClass), params);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new JdbcRuntimeException("persistent list by class failure, entity -> " + entityClass.getName());
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
            log.error(e.getMessage(), e);
            throw new JdbcRuntimeException("persistent get value failure, entity -> " + this.getClass().getName());
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
            log.error(e.getMessage(), e);
            throw new JdbcRuntimeException("persistent list value failure, entity -> " + this.getClass().getName());
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
            showSql(sql, params);
        }
    }

    protected Map<String, Object> getMap(String sql, Object... params) {
        Connection connection = getConnection();
        try {
            return queryRunner.query(connection, sql, new MapHandler(), params);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new JdbcRuntimeException("persistent get map failure, entity -> " + this.getClass().getName());
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
            showSql(sql, params);
        }
    }

    protected List<Map<String, Object>> listMap(String sql, Object... params) {
        Connection connection = getConnection();
        try {
            return queryRunner.query(connection, sql, new MapListHandler(), params);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new JdbcRuntimeException("persistent list map failure, entity -> " + this.getClass().getName());
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
            throw new JdbcRuntimeException("persistent not execute initialize method, entity -> " + this.getClass().getName());
        try {
            return dataSource.getConnection();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new JdbcRuntimeException("persistent get connection failure, entity -> " + this.getClass().getName());
        }
    }

    private void showSql(String sql, Object... params) {
        if (log.isDebugEnabled())
            log.debug("{} {}", sql, params);
    }

}
