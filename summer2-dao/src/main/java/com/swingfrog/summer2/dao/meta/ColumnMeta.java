package com.swingfrog.summer2.dao.meta;

import com.swingfrog.summer2.dao.constant.ColumnType;

import java.lang.reflect.Field;

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

    public ColumnMeta(String name, String comment, ColumnType type, boolean readOnly, int length, String defaultValue
            , Field field, String fieldName) {
        this.name = name;
        this.comment = comment;
        this.type = type;
        this.readOnly = readOnly;
        this.length = length;
        this.defaultValue = defaultValue;
        this.field = field;
        this.fieldName = fieldName;
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

    public static final class Builder {
        private String name;
        private String comment;
        private ColumnType type;
        private boolean readOnly;
        private int length;
        private String defaultValue;
        private Field field;
        private String fieldName;

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

        public ColumnMeta build() { return new ColumnMeta(name, comment, type, readOnly, length, defaultValue, field, fieldName); }
    }
}
