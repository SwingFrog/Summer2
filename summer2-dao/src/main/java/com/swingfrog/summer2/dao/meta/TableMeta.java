package com.swingfrog.summer2.dao.meta;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: toke
 */
public class TableMeta {

    private final String name;
    private final String comment;
    private final String charset;
    private final String collate;

    private final Set<IndexMeta> indexMetas;
    private final PrimaryKeyMeta primaryKeyMeta;
    private final List<ColumnMeta> columnMetas;
    private final Map<String, ColumnMeta> fieldToColumnMetas;

    public TableMeta(String name, String comment, String charset, String collate, Set<IndexMeta> indexMetas,
                     PrimaryKeyMeta primaryKeyMeta, List<ColumnMeta> columnMetas,
                     Map<String, ColumnMeta> fieldToColumnMetas) {
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

    public Set<IndexMeta> getIndexMetas() {
        return indexMetas;
    }

    public PrimaryKeyMeta getPrimaryKeyMeta() {
        return primaryKeyMeta;
    }

    public List<ColumnMeta> getColumnMetas() {
        return columnMetas;
    }

    public Map<String, ColumnMeta> getFieldToColumnMetas() {
        return fieldToColumnMetas;
    }

    public static final class Builder {
        private String name;
        private String comment;
        private String charset;
        private String collate;
        private Set<IndexMeta> indexMetas;
        private PrimaryKeyMeta primaryKeyMeta;
        private List<ColumnMeta> columnMetas;
        private Map<String, ColumnMeta> fieldToColumnMetas;

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

        public Builder indexMetas(Set<IndexMeta> indexMetas) {
            this.indexMetas = indexMetas;
            return this;
        }

        public Builder primaryKeyMeta(PrimaryKeyMeta primaryKeyMeta) {
            this.primaryKeyMeta = primaryKeyMeta;
            return this;
        }

        public Builder columnMetas(List<ColumnMeta> columnMetas) {
            this.columnMetas = columnMetas;
            return this;
        }

        public Builder fieldToColumnMetas(Map<String, ColumnMeta> fieldToColumnMetas) {
            this.fieldToColumnMetas = fieldToColumnMetas;
            return this;
        }

        public TableMeta build() { return new TableMeta(name, comment, charset, collate, indexMetas, primaryKeyMeta, columnMetas, fieldToColumnMetas); }
    }
}
