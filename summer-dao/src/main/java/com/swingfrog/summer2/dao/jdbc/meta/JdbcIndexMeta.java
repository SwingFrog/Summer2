package com.swingfrog.summer2.dao.jdbc.meta;

import com.google.common.collect.ImmutableSet;
import com.swingfrog.summer2.dao.constant.IndexType;
import com.swingfrog.summer2.dao.meta.IndexMeta;

import java.util.Objects;

/**
 * @author: toke
 */
public class JdbcIndexMeta {

    private final String name;
    private final ImmutableSet<String> columns;
    private final IndexType type;

    public JdbcIndexMeta(String name, ImmutableSet<String> columns, IndexType type) {
        this.name = name;
        this.columns = columns;
        this.type = type;
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

    public static final class Builder {
        private String name;
        private ImmutableSet<String> columns;
        private IndexType type;

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

        public JdbcIndexMeta build() { return new JdbcIndexMeta(name, columns, type); }
    }

    public boolean isSame(IndexMeta indexMeta) {
        if (!name.equals(indexMeta.getName()))
            return false;
        if (columns.size() != indexMeta.getColumns().size())
            return false;
        if (!columns.containsAll(indexMeta.getColumns()))
            return false;
        return type == indexMeta.getType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JdbcIndexMeta that = (JdbcIndexMeta) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(columns, that.columns) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, columns, type);
    }

}
