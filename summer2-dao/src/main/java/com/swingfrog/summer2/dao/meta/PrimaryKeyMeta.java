package com.swingfrog.summer2.dao.meta;

/**
 * @author: toke
 */
public class PrimaryKeyMeta extends ColumnMeta {

    private final boolean autoIncrement;

    public PrimaryKeyMeta(ColumnMeta columnMeta, boolean autoIncrement) {
        super(columnMeta.getName(),
                columnMeta.getComment(),
                columnMeta.getType(),
                columnMeta.isReadOnly(),
                columnMeta.getLength(),
                columnMeta.getDefaultValue(),
                columnMeta.getField(),
                columnMeta.getFieldName(),
                true);
        this.autoIncrement = autoIncrement;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public static final class Builder {
        private ColumnMeta columnMeta;
        private boolean autoIncrement;

        private Builder() {}

        public static Builder newBuilder() { return new Builder(); }

        public Builder columnMeta(ColumnMeta columnMeta) {
            this.columnMeta = columnMeta;
            return this;
        }

        public Builder autoIncrement(boolean autoIncrement) {
            this.autoIncrement = autoIncrement;
            return this;
        }

        public PrimaryKeyMeta build() { return new PrimaryKeyMeta(columnMeta, autoIncrement); }
    }
}
