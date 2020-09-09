package com.swingfrog.summer2.dao.jdbc;

import com.alibaba.fastjson.JSON;
import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.dbutils.ColumnHandler;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.ServiceLoader;

public class PersistentBeanProcessor extends BeanProcessor {

    private static final ServiceLoader<ColumnHandler> columnHandlers = ServiceLoader.load(ColumnHandler.class);

    public PersistentBeanProcessor(Map<String, String> columnToPropertyOverrides) {
        super(columnToPropertyOverrides);
    }

    @Override
    protected Object processColumn(ResultSet rs, int index, Class<?> propType) throws SQLException {
        Object retval = rs.getObject(index);

        if ( !propType.isPrimitive() && retval == null ) {
            return null;
        }

        boolean match = false;
        for (ColumnHandler handler : columnHandlers) {
            if (handler.match(propType)) {
                retval = handler.apply(rs, index);
                match = true;
                break;
            }
        }

        if (!match && propType != Date.class && propType != Enum.class) {
            retval = JSON.parseObject(rs.getString(index), (Type) propType);
        }

        return retval;
    }

}
