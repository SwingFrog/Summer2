package com.swingfrog.summer2.dao.meta;

import com.swingfrog.summer2.dao.constant.ColumnType;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * @author: toke
 */
public class ColumnMeta {

    private final String name;
    private final String comment;
    private final ColumnType type;
    private final boolean readOnly;
    private final int length;
    private final String defaultValue;
    private final Field field;
    private final String fieldName;
    private final boolean notNull;

    public ColumnMeta(String name, String comment, ColumnType type, boolean readOnly, int length, String defaultValue
            , Field field, String fieldName, boolean notNull) {
        this.name = name;
        this.comment = comment;
        this.type = type;
        this.readOnly = readOnly;
        this.length = length;
        this.defaultValue = defaultValue;
        this.field = field;
        this.fieldName = fieldName;
        this.notNull = notNull;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public ColumnType getType() {
        return type;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public int getLength() {
        return length;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public Field getField() {
        return field;
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean isNotNull() {
        return notNull;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnMeta that = (ColumnMeta) o;
        return readOnly == that.readOnly &&
                length == that.length &&
                notNull == that.notNull &&
                Objects.equals(name, that.name) &&
                Objects.equals(comment, that.comment) &&
                type == that.type &&
                Objects.equals(defaultValue, that.defaultValue) &&
                Objects.equals(field, that.field) &&
                Objects.equals(fieldName, that.fieldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, comment, type, readOnly, length, defaultValue, field, fieldName, notNull);
    }

    public static final class Builder {
        private String name;
        private String comment;
        private ColumnType type;
        private boolean readOnly;
        private int length;
        private String defaultValue;
        private Field field;
        private String fieldName;
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

        public Builder readOnly(boolean readOnly) {
            this.readOnly = readOnly;
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

        public Builder field(Field field) {
            this.field = field;
            return this;
        }

        public Builder fieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        public Builder notNull(boolean notNull) {
            this.notNull = notNull;
            return this;
        }

        public ColumnMeta build() { return new ColumnMeta(name, comment, type, readOnly, length, defaultValue, field, fieldName, notNull); }
    }

}
