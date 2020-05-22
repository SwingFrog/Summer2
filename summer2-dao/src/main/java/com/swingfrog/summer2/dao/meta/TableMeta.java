package com.swingfrog.summer2.dao.meta;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * @author: toke
 */
public class TableMeta {

    private final String name;
    private final String comment;
    private final String charset;
    private final String collate;

    private final ImmutableSet<IndexMeta> indexMetas;
    private final PrimaryKeyMeta primaryKeyMeta;
    private final ImmutableList<ColumnMeta> columnMetas;
    private final ImmutableMap<String, ColumnMeta> fieldToColumnMetas;

    public TableMeta(String name, String comment, String charset, String collate, ImmutableSet<IndexMeta> indexMetas,
                     PrimaryKeyMeta primaryKeyMeta, ImmutableList<ColumnMeta> columnMetas, ImmutableMap<String,
            ColumnMeta> fieldToColumnMetas) {
        this.name = name;
        this.comment = comment;
        this.charset = charset;
        this.collate = collate;
        this.indexMetas = indexMetas;
        this.primaryKeyMeta = primaryKeyMeta;
        this.columnMetas = columnMetas;
        this.fieldToColumnMetas = fieldToColumnMetas;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public String getCharset() {
        return charset;
    }

    public String getCollate() {
        return collate;
    }

    public ImmutableSet<IndexMeta> getIndexMetas() {
        return indexMetas;
    }

    public PrimaryKeyMeta getPrimaryKeyMeta() {
        return primaryKeyMeta;
    }

    public ImmutableList<ColumnMeta> getColumnMetas() {
        return columnMetas;
    }

    public ImmutableMap<String, ColumnMeta> getFieldToColumnMetas() {
        return fieldToColumnMetas;
    }

    public static final class Builder {
        private String name;
        private String comment;
        private String charset;
        private String collate;
        private ImmutableSet<IndexMeta> indexMetas;
        private PrimaryKeyMeta primaryKeyMeta;
        private ImmutableList<ColumnMeta> columnMetas;
        private ImmutableMap<String, ColumnMeta> fieldToColumnMetas;

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

        public Builder charset(String charset) {
            this.charset = charset;
            return this;
        }

        public Builder collate(String collate) {
            this.collate = collate;
            return this;
        }

        public Builder indexMetas(ImmutableSet<IndexMeta> indexMetas) {
            this.indexMetas = indexMetas;
            return this;
        }

        public Builder primaryKeyMeta(PrimaryKeyMeta primaryKeyMeta) {
            this.primaryKeyMeta = primaryKeyMeta;
            return this;
        }

        public Builder columnMetas(ImmutableList<ColumnMeta> columnMetas) {
            this.columnMetas = columnMetas;
            return this;
        }

        public Builder fieldToColumnMetas(ImmutableMap<String, ColumnMeta> fieldToColumnMetas) {
            this.fieldToColumnMetas = fieldToColumnMetas;
            return this;
        }

        public TableMeta build() { return new TableMeta(name, comment, charset, collate, indexMetas, primaryKeyMeta, columnMetas, fieldToColumnMetas); }
    }
}
