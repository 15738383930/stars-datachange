package com.stars.datachange.model.code;


import org.apache.commons.lang3.StringUtils;

public enum OldDefaultBaseCode implements BaseCode {

    /**
     *
     */
    DEFAULT("default", "-1", "默认"),

    ;

    private final String t;
    private final String k;
    private final String v;

    OldDefaultBaseCode(String t, String k, String v) {
        this.t = t;
        this.k = k;
        this.v = v;
    }

    @Override
    public String getT() {
        return t;
    }

    public String getK() {
        return k;
    }

    public String getV() {
        return v;
    }

    public static String getValue(String t, String k) {
        if (StringUtils.isNotEmpty(k)) {
            OldDefaultBaseCode[] values = OldDefaultBaseCode.values();
            for (OldDefaultBaseCode code : values) {
                if (code.getT().equals(t) && code.getK().equals(k)) {
                    return code.getV();
                }
            }
        }
        return k;
    }

}
