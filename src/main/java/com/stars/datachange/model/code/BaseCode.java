package com.stars.datachange.model.code;

import com.stars.datachange.utils.StringUtils;
import lombok.SneakyThrows;

import java.util.Objects;

/**
 * 字典规范<p>
 *     <p>凡是字典枚举模型，都需实现该规范。</p>
 *     <p>旧版用例参考：{@link OldDefaultBaseCode}</p>
 *     <p>新版用例参考：{@link DefaultBaseCode}</p>
 * @author Hao.
 * @version 1.3
 * @since 2022/5/28 13:07
 */
public interface BaseCode {

    /**
     * 属性名<p>
     *     将在v2.0及以后的版本替换关键字default为abstract
     * @author Hao.
     * @since 2022/5/28 13:00
     * @return 属性名
     */
    default String t() {
        return null;
    }

    /**
     * 属性代码<p>
     *     将在v2.0及以后的版本替换关键字default为abstract
     * @author Hao.
     * @since 2022/5/28 13:00
     * @return 属性代码
     */
    default String k() {
        return null;
    }

    /**
     * 属性值<p>
     *     将在v2.0及以后的版本替换关键字default为abstract
     * @author Hao.
     * @since  2022/5/28 13:00
     * @return 属性值
     */
    default String v() {
        return null;
    }

    /**
     * 属性名（将在v2.0及以后的版本中删除）<p>
     *     替代：{@link BaseCode#t()}
     * @author Hao.
     * @since  2022/5/28 13:00
     * @return 属性名
     */
    @Deprecated
    default String getT() {
        return null;
    }

    /**
     * 属性代码（将在v2.0及以后的版本中删除）<p>
     *     替代：{@link BaseCode#k()}
     * @author Hao.
     * @since 2022/5/28 13:00
     * @return 属性代码
     */
    @Deprecated
    default String getK() {
        return null;
    }

    /**
     * 属性值（将在v2.0及以后的版本中删除）<p>
     *     替代：{@link BaseCode#v()}
     * @author Hao.
     * @since 2022/5/28 13:00
     * @return 属性值
     */
    @Deprecated
    default String getV() {
        return null;
    }

    static String value(Class<? extends Enum> modelCode, String t, String k) {
        return change(modelCode, t, k, null);
    }

    static String key(Class<? extends Enum> modelCode, String t, String v) {
        return change(modelCode, t, null, v);
    }

    @SneakyThrows
    static String change(Class<? extends Enum> modelCode, String t, String k, String v) {
        final boolean empty = StringUtils.isEmpty(k) && StringUtils.isEmpty(v);
        final boolean notEmpty = StringUtils.isNotEmpty(k) && StringUtils.isNotEmpty(v);
        if (empty || notEmpty) {
            return null;
        }

        for (Enum o : modelCode.getEnumConstants()) {
            Object finalt = modelCode.getMethod("t").invoke(o);
            Object finalk = modelCode.getMethod("k").invoke(o);
            Object finalv = modelCode.getMethod("v").invoke(o);

            // Pending delete
            {
                finalt = Objects.isNull(finalt) ? modelCode.getMethod("getT").invoke(o) : finalt;
                finalk = Objects.isNull(finalk) ? modelCode.getMethod("getK").invoke(o) : finalk;
                finalv = Objects.isNull(finalv) ? modelCode.getMethod("getV").invoke(o) : finalv;
            }

            if (!Objects.equals(StringUtils.valueOf(finalt), t)) {
                continue;
            }

            if (StringUtils.isNotEmpty(k) && k.equals(StringUtils.valueOf(finalk))) {
                return StringUtils.valueOf(finalv);
            }

            if (StringUtils.isNotEmpty(v) && v.equals(StringUtils.valueOf(finalv))) {
                return StringUtils.valueOf(finalk);
            }
        }
        return StringUtils.isEmpty(k) ? v : k;
    }

    /**
     * 获取属性值（可自定义）<p>
     *  示例：{@link DefaultBaseCode}
     * @author Hao.
     * @since 2022/5/26 15:58
     * @param k 属性代码
     * @return 属性值
     */
    default String value(String k) {
        return k;
    }

    /**
     * 获取属性代码（可自定义）<p>
     *  示例：{@link DefaultBaseCode}
     * @author Hao.
     * @since 2022/5/26 15:58
     * @param v 属性值
     * @return 属性代码
     */
    default String key(String v) {
        return v;
    }
}
