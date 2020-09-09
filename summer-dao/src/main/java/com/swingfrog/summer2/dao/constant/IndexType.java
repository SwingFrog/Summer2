package com.swingfrog.summer2.dao.constant;

/**
 * @author: toke
 */
public enum IndexType {

    NORMAL(""),
    UNIQUE("UNIQUE"),
    FULLTEXT("FULLTEXT");

    private final String value;

    IndexType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
