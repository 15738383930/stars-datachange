package com.stars.datachange.model.code;

/**
 * 女朋友字典枚举
 * @author zhouhao
 * @since  2021/7/27 10:35
 */
public enum GirlfriendCode implements BaseCode {

    /**
     * 类型
     */
    TYPE_1("type", "1", "安静"),
    TYPE_2("type", "2", "火辣"),
    TYPE_3("type", "3", "清爽"),

    /**
     * 喜欢的食物
     */
    FAVORITE_FOOD_1("favoriteFood", "1", "牛奶"),
    FAVORITE_FOOD_2("favoriteFood", "2", "香蕉"),
    FAVORITE_FOOD_3("favoriteFood", "3", "香肠"),
    FAVORITE_FOOD_4("favoriteFood", "4", "黄瓜"),
    FAVORITE_FOOD_5("favoriteFood", "5", "火锅"),

    /**
     * 旅游地
     */
    TOURIST_PLACE_2("touristPlace", "2", "夏威夷"),
    TOURIST_PLACE_4("touristPlace", "4", "摩洛哥"),
    TOURIST_PLACE_8("touristPlace", "8", "马尔代夫"),

    ;

    private final String t;
    private final String k;
    private final String v;

    GirlfriendCode(String t, String k, String v) {
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
