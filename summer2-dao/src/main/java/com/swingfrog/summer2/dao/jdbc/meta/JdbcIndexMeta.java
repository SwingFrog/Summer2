package com.swingfrog.summer2.dao.jdbc.meta;

import com.swingfrog.summer2.dao.constant.IndexType;
import com.swingfrog.summer2.dao.meta.IndexMeta;

import java.util.Set;

/**
 * @author: toke
 */
public class JdbcIndexMeta {

    private final String name;
    private final Set<String> columns;
    private final IndexType type;

    public JdbcIndexMeta(String name, Set<String> columns, IndexType type) {
        this.name = name;
        this.columns = columns;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Set<String> getColumns() {
        return columns;
    }

    public IndexType getType() {
        return type;
    }

    public static final class Builder {
        private String name;
        private Set<String> columns;
        private IndexType type;

        private Builder() {}

        public static Builder newBuilder() { return new Builder(); }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder columns(Set<String> columns) {
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
}
