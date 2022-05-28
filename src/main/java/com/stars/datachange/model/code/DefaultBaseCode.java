package com.stars.datachange.model.code;


public enum DefaultBaseCode implements BaseCode {

    /**
     *
     */
    DEFAULT("default", "-1", "默认"),

    ;

    private final String t;
    private final String k;
    private final String v;

    DefaultBaseCode(String t, String k, String v) {
        this.t = t;
        this.k = k;
        this.v = v;
    }

    @Override
    public String t() {
        return t;
    }

    @Override
    public String k() {
        return k;
    }

    @Override
    public String v() {
        return v;
    }

}
