package com.stars.datachange.utils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Bean工具类
 * @Author zhou
 * @Date 2021/9/9 15:16
 */
public class BeanUtils extends org.springframework.beans.BeanUtils {

    /**
     * map转对象
     * @param map
     * @param beanClass
     * @return java.lang.Object
     * @author zhouhao
     * @since  2021/9/9 15:16
     */
    public static Object mapToBean(Map<String, Object> map, Class<?> beanClass) throws Exception {
        if (map == null) {
            return null;
        }

        Object obj = beanClass.newInstance();

        BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor property : propertyDescriptors) {
            Method setter = property.getWriteMethod();
            if (setter != null) {
                setter.invoke(obj, map.get(property.getName()));
            }
        }

        return obj;
    }

    /**
     * 对象转map
     * @param obj
     * @return java.util.Map<?,?>
     * @author zhouhao
     * @since  2021/9/9 15:22
     */
    public static Map<String, Object> beanToMap(Object obj) throws Exception {
        if(obj == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();

        BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor property : propertyDescriptors) {
            String key = property.getName();
            if (key.compareToIgnoreCase("class") == 0) {
                continue;
            }
            Method getter = property.getReadMethod();
            Object value = getter!=null ? getter.invoke(obj) : null;
            map.put(key, value);
        }

        return map;
    }
}
