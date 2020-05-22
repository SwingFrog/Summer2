package com.swingfrog.summer2.dao.meta;

import com.google.common.collect.ImmutableSet;
import com.swingfrog.summer2.dao.constant.IndexType;

/**
 * @author: toke
 */
public class IndexMeta {

    private final String name;
    private final ImmutableSet<String> columns;
    private final IndexType type;
    private final ImmutableSet<String> fields;

    public IndexMeta(String name, ImmutableSet<String> columns, IndexType type, ImmutableSet<String> fields) {
        this.name = name;
        this.columns = columns;
        this.type = type;
        this.fields = fields;
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

    public static final class Builder {
        private String name;
        private ImmutableSet<String> columns;
        private IndexType type;
        private ImmutableSet<String> fields;

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

        public IndexMeta build() { return new IndexMeta(name, columns, type, fields); }
    }
}
