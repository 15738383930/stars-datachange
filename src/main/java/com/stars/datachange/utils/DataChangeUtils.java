package com.stars.datachange.utils;

import com.stars.datachange.annotation.ChangeModel;
import com.stars.datachange.exception.ChangeModelException;
import com.stars.datachange.exception.ReentrantChangeModelPropertyException;
import com.stars.datachange.model.code.BaseCode;
import com.stars.datachange.model.response.DataChangeContrastResult;
import com.stars.datachange.module.Compatible;
import com.stars.datachange.module.DataDictionary;
import com.stars.datachange.module.DefaultCompatible;
import com.stars.datachange.module.Process;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.*;

import static com.stars.datachange.module.Process.*;

/**
 * 数据转换工具类
 * @author zhou
 * @since 2021/9/6 10:00
 */
@Slf4j
@Component
public final class DataChangeUtils {

    private static DataDictionary dataDictionary;

    private static Compatible compatible;

    /** 是否反转（V转K） 默认false */
    public static final ThreadLocal<Boolean> ROLLBACK = ThreadLocal.withInitial(() -> false);

    public DataChangeUtils(DataDictionary dataDictionary, Compatible compatible){
        DataChangeUtils.dataDictionary = dataDictionary;
        DataChangeUtils.compatible = compatible;
    }

    /**
     * 数据转换
     * <br>
     * <br>调用此方法，可以使你的属性code，转换为相对应的文字；
     * <br>支持多选的属性code、 支持位运算的属性code、支持属性code自定义分隔符转换
     * @param data 数据集
     * @return java.util.Map
     * @author zhouhao
     * @since  2021/9/7 11:14
     */
    public static Map<String, Object> dataChange(Object data) {
        return dataChange(data, false);
    }

    /**
     * 数据转换
     * <br>
     * <br>调用此方法，可以使你的属性code，转换为相对应的文字；
     * <br>支持多选的属性code、 支持位运算的属性code、支持属性code自定义分隔符转换
     * @param data 数据集
     * @param rollback 是否反转（V转K） 默认false
     * @return java.util.Map
     * @author zhouhao
     * @since  2021/9/7 11:14
     */
    public static Map<String, Object> dataChange(Object data, boolean rollback) {
        try {
            ROLLBACK.set(rollback);
            return dataChange(data, Process.create(data.getClass(), new Process(), dataDictionary));
        } finally {
            ROLLBACK.remove();
        }
    }

    /**
     * 数据转换（集合）
     * <br>
     * <br>调用此方法，可以使你的属性code，转换为相对应的文字；
     * <br>支持多选的属性code、 支持位运算的属性code、支持属性code自定义分隔符转换
     * @param data 数据集
     * @return java.util.Map
     * @author zhouhao
     * @since  2021/9/7 11:14
     */
    public static <T> List<Map<String, Object>> dataChange(Collection<T> data) {
        List<Map<String, Object>> result = new ArrayList<>();
        data.forEach(o -> result.add(dataChange(o, false)));
        return result;
    }

    /**
     * 数据转换（集合）
     * <br>
     * <br>调用此方法，可以使你的属性code，转换为相对应的文字；
     * <br>支持多选的属性code、 支持位运算的属性code、支持属性code自定义分隔符转换
     * @param data 数据集
     * @param rollback 是否反转（V转K） 默认false
     * @return java.util.Map
     * @author zhouhao
     * @since  2021/9/7 11:14
     */
    public static <T> List<Map<String, Object>> dataChange(Collection<T> data, boolean rollback) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (!CollectionUtils.isEmpty(data)) {
            data.forEach(o -> result.add(dataChange(o, rollback)));
        }
        return result;
    }

    /**
     * 数据转换（数组）
     * <br>
     * <br>调用此方法，可以使你的属性code，转换为相对应的文字；
     * <br>支持多选的属性code、 支持位运算的属性code、支持属性code自定义分隔符转换
     * @param data 数据集
     * @return java.util.Map
     * @author zhouhao
     * @since  2021/9/7 11:14
     */
    public static <T> List<Map<String, Object>> dataChange(T[] data) {
        return dataChange(Arrays.asList(data));
    }

    /**
     * 数据转换（数组）
     * <br>
     * <br>调用此方法，可以使你的属性code，转换为相对应的文字；
     * <br>支持多选的属性code、 支持位运算的属性code、支持属性code自定义分隔符转换
     * @param data 数据集
     * @param rollback 是否反转（V转K） 默认false
     * @return java.util.Map
     * @author zhouhao
     * @since  2021/9/7 11:14
     */
    public static <T> List<Map<String, Object>> dataChange(T[] data, boolean rollback) {
        return dataChange(Arrays.asList(data), rollback);
    }

    /**
     * 数据转换（转换到原对象）
     * <br>
     * <br>调用此方法，可以使你的属性code，转换为相对应的文字；
     * <br>支持多选的属性code、 支持位运算的属性code、支持属性code自定义分隔符转换
     * @param data 数据集
     * @author zhouhao
     * @since  2022/4/30 10:35
     */
    public static <T> void dataChangeToBean(T data) {
        dataChangeToBean(data, false);
    }

    /**
     * 数据转换（转换到原对象）（集合）
     * <br>
     * <br>调用此方法，可以使你的属性code，转换为相对应的文字；
     * <br>支持多选的属性code、 支持位运算的属性code、支持属性code自定义分隔符转换
     * @param data 数据集
     * @author zhouhao
     * @since  2022/4/30 10:35
     */
    public static <T> void dataChangeToBean(Collection<T> data) {
        if (!CollectionUtils.isEmpty(data)) {
            data.forEach(o -> dataChangeToBean(o, false));
        }
    }

    /**
     * 数据转换（转换到原对象）（集合）
     * <br>
     * <br>调用此方法，可以使你的属性code，转换为相对应的文字；
     * <br>支持多选的属性code、 支持位运算的属性code、支持属性code自定义分隔符转换
     * @param data 数据集
     * @param rollback 是否反转（V转K） 默认false
     * @author zhouhao
     * @since  2022/4/30 10:35
     */
    public static <T> void dataChangeToBean(Collection<T> data, boolean rollback) {
        if (!CollectionUtils.isEmpty(data)) {
            data.forEach(o -> dataChangeToBean(o, rollback));
        }
    }

    /**
     * 数据转换（转换到原对象）（数组）
     * <br>
     * <br>调用此方法，可以使你的属性code，转换为相对应的文字；
     * <br>支持多选的属性code、 支持位运算的属性code、支持属性code自定义分隔符转换
     * @param data 数据集
     * @author zhouhao
     * @since  2022/4/30 10:35
     */
    public static <T> void dataChangeToBean(T[] data) {
        if (Objects.nonNull(data)) {
            for (T o : data) {
                dataChangeToBean(o, false);
            }
        }
    }

    /**
     * 数据转换（转换到原对象）（数组）
     * <br>
     * <br>调用此方法，可以使你的属性code，转换为相对应的文字；
     * <br>支持多选的属性code、 支持位运算的属性code、支持属性code自定义分隔符转换
     * @param data 数据集
     * @param rollback 是否反转（V转K） 默认false
     * @author zhouhao
     * @since  2022/4/30 10:35
     */
    public static <T> void dataChangeToBean(T[] data, boolean rollback) {
        if (Objects.nonNull(data)) {
            for (T o : data) {
                dataChangeToBean(o, rollback);
            }
        }
    }

    /**
     * 数据转换（转换到原对象）
     * <br>
     * <br>调用此方法，可以使你的属性code，转换为相对应的文字；
     * <br>支持多选的属性code、 支持位运算的属性code、支持属性code自定义分隔符转换
     * @param data 数据集
     * @param rollback 是否反转（V转K） 默认false
     * @author zhouhao
     * @since  2022/4/30 10:35
     */
    public static <T> void dataChangeToBean(T data, boolean rollback) {
        try {
            ROLLBACK.set(rollback);
            dataChangeToBean(data, Process.create(data.getClass(), new Process(), dataDictionary));
        } finally {
            ROLLBACK.remove();
        }
    }

    private static Map<String, Object> dataChange(Object data, Process process) {
        final Class<?> dataClass = data.getClass();

        Map<String, Object> result = BeanUtils.beanToMap(data);
        if(Objects.isNull(result)) {
            return new HashMap<>();
        }

        if (Objects.isNull(compatible)) {
            compatible = DefaultCompatible.get();
        }
        compatible.run(dataClass, result, process.getCompatible());

        for (String key : result.keySet()) {
            if(Objects.isNull(result.get(key))){
                continue;
            }

            if(process.isIgnore(key)){
                continue;
            }

            // 重入逻辑
            if (process.isReentrant(key)) {
                final Object keyO = result.get(key);
                final Class<?> type = keyO.getClass();
                // 校验数据类型（目前只允许：对象数组、单列结合、对象）
                // 数组
                if (type.isArray()) {
                    final Object[] os = (Object[]) keyO;
                    List<Map<String, Object>> osMaps = new ArrayList<>();
                    Process process_ = null;
                    for (Object o : os) {
                        if (Objects.isNull(process_)) {
                            process_ = Process.create(o.getClass(), new Process(), dataDictionary);
                        }
                        osMaps.add(dataChange(o, process_));
                    }
                    result.put(key, osMaps);
                    continue;
                }
                // 单列集合
                if (Collection.class.isAssignableFrom(type)) {
                    final Collection<?> os = (Collection<?>) keyO;
                    List<Map<String, Object>> osMaps = new ArrayList<>();
                    Process process_ = null;
                    for (Object o : os) {
                        if (Objects.isNull(process_)) {
                            process_ = Process.create(o.getClass(), new Process(), dataDictionary);
                        }
                        osMaps.add(dataChange(o, process_));
                    }
                    result.put(key, osMaps);
                    continue;
                }
                // 对象
                if (type.isAnnotationPresent(ChangeModel.class)) {
                    result.put(key, dataChange(keyO, Process.create(keyO.getClass(), new Process(), dataDictionary)));
                    continue;
                }
                // 其他数据类型
                throw new ReentrantChangeModelPropertyException(String.format("Misused @ReentrantChangeModelProperty, from attributes %s. @ReentrantChangeModelProperty are use only allowed on properties such as array, java.util.Collection, custom class, etc. by @ReentrantChangeModelProperty the properties of the markings, need to mark @ChangeModel on the corresponding data model.", key));
            }

            // 数据转换注解仅作为标识，跳过数据转换逻辑
            if(process.getSource().equals(ChangeModel.Source.NONE)) {
                continue;
            }

            // 字段别名
            String keyAlias = StringUtils.isEmpty(process.getAlias().get(key)) ? key : process.getAlias().get(key);

            // 位运算转换
            if(process.isBitOperation(key)){
                if (ROLLBACK.get()) {
                    log.warn("The value of bit operations does not support inversion!");
                    continue;
                }
                if (process.getSource().equals(ChangeModel.Source.ENUM)) {
                    result.put(key, splitConversion(process.getModelCode(), keyAlias, bitOperation(result.get(key))));
                }
                if (process.getSource().equals(ChangeModel.Source.DB)) {
                    result.put(key, splitConversion(process.getDictionaryResult(), keyAlias, bitOperation(result.get(key))));
                }
                continue;
            }

            // 分割转换
            if(process.isSplit(key)){
                if (process.getSource().equals(ChangeModel.Source.ENUM)) {
                    result.put(key, splitConversion(process.getModelCode(), keyAlias, result.get(key).toString(), process.getSplitDelimiter().get(key)));
                }
                if (process.getSource().equals(ChangeModel.Source.DB)) {
                    result.put(key, splitConversion(process.getDictionaryResult(), keyAlias, result.get(key).toString(), process.getSplitDelimiter().get(key)));
                }
                continue;
            }

            // 转换
            {
                if (process.getSource().equals(ChangeModel.Source.ENUM)) {
                    String o = ROLLBACK.get() ? BaseCode.key(process.getModelCode(), keyAlias, result.get(key).toString()) : BaseCode.value(process.getModelCode(), keyAlias, result.get(key).toString());
                    result.put(key, o);
                }
                if (process.getSource().equals(ChangeModel.Source.DB)) {
                    result.put(key, getValue(process.getDictionaryResult(), keyAlias, result.get(key).toString()));
                }
                continue;
            }
        }

        return result;
    }

    @SneakyThrows
    private static <T> void dataChangeToBean(T data, Process process) {
        final Class<?> dataClass = data.getClass();

        final List<Field> fields = getFields(dataClass, new ArrayList<>());
        for(Field field : fields){
            if(!field.isAccessible()){
                field.setAccessible(true);
            }

            String name = field.getName();
            Object value = field.get(data);

            if(Objects.isNull(value)){
                continue;
            }

            if(process.isIgnore(name)){
                continue;
            }

            // 重入逻辑
            if (process.isReentrant(name)) {
                final Class<?> type = field.getType();
                // 校验数据类型（目前只允许：对象数组、单列结合、对象）
                // 数组
                if (type.isArray()) {
                    final Object[] os = (Object[]) value;
                    Process process_ = null;
                    for (Object o : os) {
                        if (Objects.isNull(process_)) {
                            process_ = Process.create(o.getClass(), new Process(), dataDictionary);
                        }
                        dataChangeToBean(o, process_);
                    }
                    continue;
                }
                // 单列集合
                if (Collection.class.isAssignableFrom(type)) {
                    final Collection<?> os = (Collection<?>) value;
                    Process process_ = null;
                    for (Object o : os) {
                        if (Objects.isNull(process_)) {
                            process_ = Process.create(o.getClass(), new Process(), dataDictionary);
                        }
                        dataChangeToBean(o, process_);
                    }
                    continue;
                }
                // 对象
                if (type.isAnnotationPresent(ChangeModel.class)) {
                    dataChangeToBean(value, Process.create(value.getClass(), new Process(), dataDictionary));
                    continue;
                }
                // 其他数据类型
                throw new ReentrantChangeModelPropertyException(String.format("Misused @ReentrantChangeModelProperty, from attributes %s. @ReentrantChangeModelProperty are use only allowed on properties such as array, java.util.Collection, custom class, etc. by @ReentrantChangeModelProperty the properties of the markings, need to mark @ChangeModel on the corresponding data model.", name));
            }

            // 数据转换注解仅作为标识，跳过数据转换逻辑
            if(process.getSource().equals(ChangeModel.Source.NONE)) {
                continue;
            }

            // 映射后的字段
            Field mappedField = getMappedField(process, dataClass, field);
            if (Objects.isNull(mappedField)) continue;

            // 字段别名
            String alias = StringUtils.isEmpty(process.getAlias().get(name)) ? name : process.getAlias().get(name);

            // 位运算转换
            if(process.isBitOperation(name)){
                if (ROLLBACK.get()) {
                    log.warn("The value of bit operations does not support inversion!");
                    continue;
                }
                if (process.getSource().equals(ChangeModel.Source.ENUM)) {
                    mappedField.set(data, splitConversion(process.getModelCode(), alias, bitOperation(value)));
                }
                if (process.getSource().equals(ChangeModel.Source.DB)) {
                    mappedField.set(data, splitConversion(process.getDictionaryResult(), alias, bitOperation(value)));
                }
                continue;
            }

            // 分割转换
            if(process.isSplit(name)){
                if (process.getSource().equals(ChangeModel.Source.ENUM)) {
                    mappedField.set(data, splitConversion(process.getModelCode(), alias, value.toString(), process.getSplitDelimiter().get(name)));
                }
                if (process.getSource().equals(ChangeModel.Source.DB)) {
                    mappedField.set(data, splitConversion(process.getDictionaryResult(), alias, value.toString(), process.getSplitDelimiter().get(name)));
                }
                continue;
            }

            // 转换
            {
                if (process.getSource().equals(ChangeModel.Source.ENUM)) {
                    final String o = ROLLBACK.get() ? BaseCode.key(process.getModelCode(), alias, value.toString()) : BaseCode.value(process.getModelCode(), alias, value.toString());
                    mappedField.set(data, o);
                }
                if (process.getSource().equals(ChangeModel.Source.DB)) {
                    mappedField.set(data, getValue(process.getDictionaryResult(), alias, value.toString()));
                }
                continue;
            }
        }
    }

    /**
     * 数据转换对比
     * <br>
     * <br>调用此方法，可以找出两个对象的差异，并收集差异
     * @param oldData 变更前的数据
     * @param newData 变更后的数据
     * @return 数据转换对比后的结果集
     */
    public static List<DataChangeContrastResult> dataContrast(Object oldData, Object newData) {
        List<DataChangeContrastResult> result = new ArrayList<>();

        final Process process = Process.create(oldData.getClass(), new Process(), dataDictionary);

        if (process.getChangeModel().quick()) {
            throw new ChangeModelException("This function does not support shortcut mode temporarily!");
        }

        try {
            ROLLBACK.set(false);
            Map<String, Object> oldData_ = dataChange(oldData, process);
            Map<String, Object> newData_ = dataChange(newData, process);

            oldData_.keySet().forEach(key -> {
                if(process.isSkipComparison(key)) {
                    return;
                }
                Object old_ = oldData_.get(key);
                Object new_ = newData_.get(key);
                if (!Objects.equals(old_, new_)) {
                    result.add(DataChangeContrastResult.builder().name(process.getChineseEnglish().get(key)).oldData(old_).newData(new_).build());
                }
            });
        } finally {
            ROLLBACK.remove();
        }
        return result;
    }
}
