package com.stars.datachange.utils;

import java.util.Objects;

public class StringUtils extends org.apache.commons.lang3.StringUtils {

    public static String valueOf(Object o){
        return Objects.isNull(o) ? null : o.toString();
    }

    public static String other(String value, String other){
        return isEmpty(value) ? other : value;
    }
}
