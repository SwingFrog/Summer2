package com.swingfrog.summer2.dao.jdbc.meta;

import com.swingfrog.summer2.dao.constant.ColumnType;
import com.swingfrog.summer2.dao.meta.ColumnMeta;

import java.util.Objects;

/**
 * @author: toke
 */
public class JdbcColumnMeta {

    private final String name;
    private final String comment;
    private final ColumnType type;
    private final int length;
    private final String defaultValue;
    private final boolean notNull;

    public JdbcColumnMeta(String name, String comment, ColumnType type, int length, String defaultValue,
                          boolean notNull) {
        this.name = name;
        this.comment = comment;
        this.type = type;
        this.length = length;
        this.defaultValue = defaultValue;
        this.notNull = notNull;
    }

    public static final class Builder {
        private String name;
        private String comment;
        private ColumnType type;
        private int length;
        private String defaultValue;
        private boolean notNull;

        private Builder() {}

        public static Builder newBuilder() { return new Builder(); }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder type(ColumnType type) {
            this.type = type;
            return this;
        }

        public Builder length(int length) {
            this.length = length;
            return this;
        }

        public Builder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder notNull(boolean notNull) {
            this.notNull = notNull;
            return this;
        }

        public JdbcColumnMeta build() { return new JdbcColumnMeta(name, comment, type, length, defaultValue, notNull); }
    }

    public boolean isSameName(ColumnMeta columnMeta) {
        return name.equals(columnMeta.getName());
    }

    public boolean isSame(ColumnMeta columnMeta) {
        if (!name.equals(columnMeta.getName()))
            return false;
        if (!comment.equals(columnMeta.getComment()))
            return false;
        if (type != columnMeta.getType())
            return false;
        if (columnMeta.getType() == ColumnType.CHAR || columnMeta.getType() == ColumnType.VARCHAR ||
                columnMeta.getType() == ColumnType.FLOAT || columnMeta.getType() == ColumnType.DOUBLE) {
            if (length != columnMeta.getLength())
                return false;
        }
        if (!defaultValue.equals(columnMeta.getDefaultValue()))
            return false;
        return notNull == columnMeta.isNotNull();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JdbcColumnMeta that = (JdbcColumnMeta) o;
        return length == that.length &&
                notNull == that.notNull &&
                Objects.equals(name, that.name) &&
                Objects.equals(comment, that.comment) &&
                type == that.type &&
                Objects.equals(defaultValue, that.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, comment, type, length, defaultValue, notNull);
    }

}
