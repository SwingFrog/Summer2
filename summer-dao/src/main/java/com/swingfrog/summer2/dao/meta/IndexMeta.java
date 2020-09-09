package com.swingfrog.summer2.dao.meta;

import com.google.common.collect.ImmutableSet;
import com.swingfrog.summer2.dao.constant.IndexType;

import java.util.Objects;

/**
 * @author: toke
 */
public class IndexMeta {

    private final String name;
    private final ImmutableSet<String> columns;
    private final IndexType type;
    private final ImmutableSet<String> fields;
    private final boolean allReadOnly;

    public IndexMeta(String name, ImmutableSet<String> columns, IndexType type, ImmutableSet<String> fields,
                     boolean allReadOnly) {
        this.name = name;
        this.columns = columns;
        this.type = type;
        this.fields = fields;
        this.allReadOnly = allReadOnly;
    }

    public String getName() {
        return name;
    }

    public ImmutableSet<String> getColumns() {
        return columns;
    }

    public IndexType getType() {
        return type;
    }

    public ImmutableSet<String> getFields() {
        return fields;
    }

    public boolean isAllReadOnly() {
        return allReadOnly;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexMeta indexMeta = (IndexMeta) o;
        return allReadOnly == indexMeta.allReadOnly &&
                Objects.equals(name, indexMeta.name) &&
                Objects.equals(columns, indexMeta.columns) &&
                type == indexMeta.type &&
                Objects.equals(fields, indexMeta.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, columns, type, fields, allReadOnly);
    }

    public static final class Builder {
        private String name;
        private ImmutableSet<String> columns;
        private IndexType type;
        private ImmutableSet<String> fields;
        private boolean allReadOnly;

        private Builder() {}

        public static Builder newBuilder() { return new Builder(); }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder columns(ImmutableSet<String> columns) {
            this.columns = columns;
            return this;
        }

        public Builder type(IndexType type) {
            this.type = type;
            return this;
        }

        public Builder fields(ImmutableSet<String> fields) {
            this.fields = fields;
            return this;
        }

        public Builder allReadOnly(boolean allReadOnly) {
            this.allReadOnly = allReadOnly;
            return this;
        }

        public IndexMeta build() { return new IndexMeta(name, columns, type, fields, allReadOnly); }
    }

}
