package com.swingfrog.summer2.dao.meta;

import com.swingfrog.summer2.dao.constant.IndexType;

import java.util.Set;

/**
 * @author: toke
 */
public class IndexMeta {

    private final String name;
    private final Set<String> fields;
    private final IndexType type;

    public IndexMeta(String name, Set<String> fields, IndexType type) {
        this.name = name;
        this.fields = fields;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Set<String> getFields() {
        return fields;
    }

    public IndexType getType() {
        return type;
    }

    public static final class Builder {
        private String name;
        private Set<String> fields;
        private IndexType type;

        private Builder() {}

        public static Builder newBuilder() { return new Builder(); }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder fields(Set<String> fields) {
            this.fields = fields;
            return this;
        }

        public Builder type(IndexType type) {
            this.type = type;
            return this;
        }

        public IndexMeta build() { return new IndexMeta(name, fields, type); }
    }
}
