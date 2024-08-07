package com.stars.datachange.utils;

import com.stars.datachange.annotation.ChangeModel;
import com.stars.datachange.annotation.ChangeModelProperty;
import com.stars.datachange.autoconfigure.StarsProperties;
import com.stars.datachange.exception.ChangeException;
import com.stars.datachange.exception.ChangeModelException;
import com.stars.datachange.exception.ChangeModelPropertyException;
import com.stars.datachange.model.code.BaseCode;
import com.stars.datachange.model.response.DataChangeContrastResult;
import com.stars.datachange.model.response.DataDictionaryResult;
import com.stars.datachange.module.Compatible;
import com.stars.datachange.module.DataDictionary;
import com.stars.datachange.module.DefaultCompatible;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据转换工具类
 * @author zhou
 * @since 2021/9/6 10:00
 */
@Slf4j
@Component
public final class DataChangeUtils {

    /** 默认映射属性的后缀 */
    private static final String[] MAPPING_SUFFIX = {"Text", "Str", "Ext"};

    private static DataDictionary dataDictionary;

    private static Compatible compatible;

    /** 是否反转（V转K） 默认false */
    private static final ThreadLocal<Boolean> ROLLBACK = ThreadLocal.withInitial(() -> false);

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
            return dataChange(data, Process.create(data.getClass(), new Process()));
        } finally {
            ROLLBACK.remove();
        }
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
            dataChangeToBean(data, Process.create(data.getClass(), new Process()));
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

            // 字段别名
            String keyAlias = StringUtils.isEmpty(process.getAlias().get(key)) ? key : process.getAlias().get(key);

            // 位运算转换
            if(process.isBitOperation(key)){
                if (ROLLBACK.get()) {
                    log.warn("The value of bit operations does not support inversion!");
                    continue;
                }
                if (process.getChangeModel().source().equals(ChangeModel.Source.ENUM)) {
                    result.put(key, splitConversion(process.getModelCode(), keyAlias, bitOperation(result.get(key))));
                }
                if (process.getChangeModel().source().equals(ChangeModel.Source.DB)) {
                    result.put(key, splitConversion(process.getDictionaryResult(), keyAlias, bitOperation(result.get(key))));
                }
                continue;
            }

            // 分割转换
            if(process.isSplit(key)){
                if (process.getChangeModel().source().equals(ChangeModel.Source.ENUM)) {
                    result.put(key, splitConversion(process.getModelCode(), keyAlias, result.get(key).toString(), process.getSplitDelimiter().get(key)));
                }
                if (process.getChangeModel().source().equals(ChangeModel.Source.DB)) {
                    result.put(key, splitConversion(process.getDictionaryResult(), keyAlias, result.get(key).toString(), process.getSplitDelimiter().get(key)));
                }
                continue;
            }

            // 转换
            {
                if (process.getChangeModel().source().equals(ChangeModel.Source.ENUM)) {
                    String o = ROLLBACK.get() ? BaseCode.key(process.getModelCode(), keyAlias, result.get(key).toString()) : BaseCode.value(process.getModelCode(), keyAlias, result.get(key).toString());
                    result.put(key, o);
                }
                if (process.getChangeModel().source().equals(ChangeModel.Source.DB)) {
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
                if (process.getChangeModel().source().equals(ChangeModel.Source.ENUM)) {
                    mappedField.set(data, splitConversion(process.getModelCode(), alias, bitOperation(value)));
                }
                if (process.getChangeModel().source().equals(ChangeModel.Source.DB)) {
                    mappedField.set(data, splitConversion(process.getDictionaryResult(), alias, bitOperation(value)));
                }
                continue;
            }

            // 分割转换
            if(process.isSplit(name)){
                if (process.getChangeModel().source().equals(ChangeModel.Source.ENUM)) {
                    mappedField.set(data, splitConversion(process.getModelCode(), alias, value.toString(), process.getSplitDelimiter().get(name)));
                }
                if (process.getChangeModel().source().equals(ChangeModel.Source.DB)) {
                    mappedField.set(data, splitConversion(process.getDictionaryResult(), alias, value.toString(), process.getSplitDelimiter().get(name)));
                }
                continue;
            }

            // 转换
            {
                if (process.getChangeModel().source().equals(ChangeModel.Source.ENUM)) {
                    final String o = ROLLBACK.get() ? BaseCode.key(process.getModelCode(), alias, value.toString()) : BaseCode.value(process.getModelCode(), alias, value.toString());
                    mappedField.set(data, o);
                }
                if (process.getChangeModel().source().equals(ChangeModel.Source.DB)) {
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

        final Process process = Process.create(oldData.getClass(), new Process());

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

    /**
     * 多选值分割转义
     * @param name 要转义的字段名
     * @param data 多选值（逗号分割）
     * @return java.lang.String 转义后的多选值（逗号分割）
     * @author zhouhao
     * @since  2020/5/29 15:01
     */
    private static String splitConversion(Class<? extends Enum> modelCode, String name, String data) {
        return splitConversion(modelCode, name, data, ",");
    }

    /**
     * 多选值分割转义
     * @param key 要转义的字段名
     * @param data 多选值（逗号分割）
     * @return java.lang.String 转义后的多选值（逗号分割）
     * @author zhouhao
     * @since  2020/5/29 15:01
     */
    private static String splitConversion(Set<DataDictionaryResult> result, String key, String data) {
        return splitConversion(result, key, data, ",");
    }

    /**
     * 多选值分割转义
     * @param name 要转义的字段名
     * @param data 多选值（regex分割）
     * @param delimiter 分割符
     * @return java.lang.String 通过delimiter转义后的多选值
     * @author zhouhao
     * @since  2020/5/29 15:01
     */
    private static String splitConversion(Class<? extends Enum> modelCode, String name, String data, String delimiter) {
        if(StringUtils.isEmpty(delimiter)){
            return splitConversion(modelCode, name, data);
        }
        if (StringUtils.isNotEmpty(data)) {
            String delimiter_ = delimiter.replace(".", "\\.").replace("|", "\\|");
            List<String> list = Arrays.asList(data.split(delimiter_));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                String s = ROLLBACK.get() ? BaseCode.key(modelCode, name, list.get(i)) : BaseCode.value(modelCode, name, list.get(i));
                if (StringUtils.isNotEmpty(s)) {
                    sb.append(s).append(list.size() - 1 == i ? "" : delimiter);
                }
            }
            return sb.toString();
        }
        return "";
    }

    /**
     * 多选值分割转义
     * @param key 要转义的字段名
     * @param data 多选值（regex分割）
     * @param delimiter 分割符
     * @return java.lang.String 通过delimiter转义后的多选值
     * @author zhouhao
     * @since  2020/5/29 15:01
     */
    private static String splitConversion(Set<DataDictionaryResult> result, String key, String data, String delimiter) {
        if(StringUtils.isEmpty(delimiter)){
            return splitConversion(result, key, data);
        }
        if (StringUtils.isNotEmpty(data)) {
            String delimiter_ = delimiter.replace(".", "\\.").replace("|", "\\|");
            List<String> list = Arrays.asList(data.split(delimiter_));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                String s = getValue(result, key, list.get(i));
                if (StringUtils.isNotEmpty(s)) {
                    sb.append(s).append(list.size() - 1 == i ? "" : delimiter);
                }
            }
            return sb.toString();
        }
        return "";
    }

    /**
     * 获取属性值
     * @param result 数据字典
     * @param name 属性名
     * @param oo 属性原值
     * @return java.lang.Object 转义后的值
     * @author zhouhao
     * @since  2020/5/29 13:16
     */
    private static String getValue(Set<DataDictionaryResult> result, String name, String oo) {
        Set<DataDictionaryResult.Map> maps = result.stream().filter(o -> o.getName().equals(name)).findFirst().map(DataDictionaryResult::getMaps).orElseGet(LinkedHashSet::new);
        if(CollectionUtils.isEmpty(maps)){
            return oo;
        }
        if (ROLLBACK.get()) {
            return maps.stream().filter(o -> o.getValue().equals(oo)).findFirst().map(DataDictionaryResult.Map::getCode).orElse(oo);
        }
        return maps.stream().filter(o -> o.getCode().equals(oo)).findFirst().map(DataDictionaryResult.Map::getValue).orElse(oo);
    }

    /**
     * 位运算
     * <br>
     * <br>实现思路：
     *      <br>把位运算的数值转换为二进制码，把二进制码反转方便后面遍历计算。
     *      <br>遍历二级制码时，遇1计算当前指针的平方，即：得出的多选值code，并添加到多选值列表中
     *      <br>遍历结束后，多选值以逗号分隔的形式返回
     * @param o 位数值
     * @return java.lang.String 运算后的多选值（逗号分隔）
     * @author zhouhao
     * @date  2020/5/28 19:58
     */
    private static String bitOperation(Object o){
        if (!RegexUtils.isNumber(o.toString())) {
            log.warn("Bit operations is not possible, Please provide a valid bit operations value!");
            return o.toString();
        }
        final int num = Integer.parseInt(o.toString());
        String data = new StringBuffer(Integer.toBinaryString(num)).reverse().toString();
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            i = data.indexOf("1", i);
            // 1 * 2的i次方
            result.add(1 << i);
        }
        return result.stream().map(Object::toString).collect(Collectors.joining(","));
    }

    /**
     * 得到映射后的字段
     * @author Hao.
     * @date 2022/4/30 11:50
     * @param process 数据转换处理模型
     * @param dataClass 数据模型
     * @param field 源字段
     * @return java.lang.reflect.Field
     */
    private static Field getMappedField(Process process, Class<?> dataClass, Field field) {
        // 原始字段名
        final String name = field.getName();

        // 映射的字段名
        String mappingName = process.getMapping().get(name);

        // 映射后的字段名
        String mappedName;

        // 映射后的字段
        Field mappedField = null;

        // 默认映射属性的后缀
        String[] mappingSuffix;
        try{
            mappingSuffix = StarsProperties.config.getMappingSuffix();
        }catch (Exception e){
            mappingSuffix = MAPPING_SUFFIX;
            log.warn("MAPPING_SUFFIX, switched back to default source. possible causes: abnormal program startup.");
        }

        // 未定义属性映射，自动匹配属性映射
        if (StringUtils.isEmpty(mappingName)) {

            // 智能匹配以Text、Str、Ext等结尾的字段
            for (String suffix : mappingSuffix) {
                try {
                    mappedField = dataClass.getDeclaredField(name + suffix);
                    break;
                } catch (NoSuchFieldException ignored) {}
            }

            // 未匹配到属性映射，去父类中匹配
            if(Objects.isNull(mappedField)){
                if(!dataClass.getSuperclass().equals(Object.class) && dataClass.getSuperclass().isAnnotationPresent(ChangeModel.class)){
                    mappedField = getMappedField(process, dataClass.getSuperclass(), field);
                }
            }

            // 未定义属性映射，使用源字段（前提：源字段为String类型）
            if(Objects.isNull(mappedField)){
                if(!field.getType().equals(String.class)){
                    return null;
                }
            }
            mappedName = name;
        }else{
            mappedName = mappingName;
        }

        // 尝试使用 源字段 或 定义的属性映射
        if(Objects.isNull(mappedField)){
            try{
                mappedField = dataClass.getDeclaredField(mappedName);
            }catch (NoSuchFieldException e) {
                if(dataClass.getSuperclass().equals(Object.class) || !dataClass.getSuperclass().isAnnotationPresent(ChangeModel.class)){
                    throw new ChangeModelPropertyException(String.format("Property mapping not found [%s] !", mappedName));
                }
                // 尝试使用父类的 源字段 或 定义的属性映射
                mappedField = getMappedField(process, dataClass.getSuperclass(), field);
            }
        }

        // 源字段/属性映射 类型必须为String
        if(!mappedField.getType().equals(String.class)){
            throw new ChangeModelPropertyException(String.format("The mapped property must be of type java.lang.String [%s] !", mappedField.getName()));
        }

        if(!mappedField.isAccessible()){
            mappedField.setAccessible(true);
        }
        return mappedField;
    }

    /**
     * 得到字段列表
     * @author Hao.
     * @date 2022/4/30 11:50
     * @param dataClass 数据模型
     * @param list 字段列表
     * @return java.lang.reflect.Field
     */
    private static List<Field> getFields(Class<?> dataClass, List<Field> list) {
        list.addAll(new ArrayList<>(Arrays.asList(dataClass.getDeclaredFields())));
        // 若父类是通用的，跳过处理阶段
        if(!dataClass.getSuperclass().equals(Object.class) && dataClass.getSuperclass().isAnnotationPresent(ChangeModel.class)){
            getFields(dataClass.getSuperclass(), list);
        }
        return list;
    }

    /**
     * 数据转换处理模型
     * @author zhouhao
     * @since  2021/9/6 17:05
     */
    @Data
    private static class Process {

        /** 数据转换模型 */
        private ChangeModel changeModel;

        /** 数据代码模型 */
        private Class<? extends Enum> modelCode;

        /** 兼容注解 */
        private Set<Class<? extends Annotation>> compatible = new HashSet<>();

        /** 数据字典 */
        private Set<DataDictionaryResult> dictionaryResult;

        /**
         * 忽略数据转换的字段
         */
        private Set<String> ignoreFields = new HashSet<>();

        /**
         * 跳过数据拆分的字段
         */
        private Set<String> splitFields = new HashSet<>();

        /**
         * 进行位运算的字段
         */
        private Set<String> bitOperationFields = new HashSet<>();

        /**
         * 跳过数据对比的字段
         */
        private Set<String> skipComparisonFields = new HashSet<>();

        /**
         * 英转中时，中文要忽略文本部分的分割符<br>
         *      例：<br><br>
         *      {@code @ChangeModelProperty(value = "女朋友类型：1-安静 2-火辣 3-清爽", chineseIgnoreDelimiter = "：")  } <br>
         *      {@code private String type1;     } <br><br>
         *      {@code @ChangeModelProperty(value = "女朋友类型 1-安静 2-火辣 3-清爽", chineseIgnoreDelimiter = " ")  } <br>
         *      {@code private Integer type2;     } <br><br>
         *      解释：<br>
         *      type1属性忽略第一个“：”后的文本，结果为【女朋友类型】（“：”为默认分割符，可不写） <br>
         *      type2属性忽略第一个“ ”后的文本，结果为【女朋友类型】 <br>
         */
        private Map<String, String> chineseIgnoreDelimiter = new HashMap<>();

        /**
         * 数据拆分时，指定的分割符<br>
         *     例：<br><br>
         *      {@code @ChangeModelProperty(value = "女朋友类型： 1-安静 2-火辣 3-清爽", split = true, delimiter = ",")  } <br>
         *      {@code private String type1;     } <br><br>
         *      {@code @ChangeModelProperty(value = "女朋友类型： 1-安静 2-火辣 3-清爽", split = true, delimiter = "|")  } <br>
         *      {@code private Integer type2;     } <br><br>
         *      解释：<br>
         *      type1属性的多选值以“,”分割，结果为【安静,火辣,清爽】（“,”为默认分割符，可不写） <br>
         *      type2属性的多选值以“|”分割，结果为【安静|火辣|清爽】 <br><br>
         *      PS：split为true的前提下，delimiter的配置才生效
         */
        private Map<String, String> splitDelimiter = new HashMap<>();

        /**
         * 中英对照集
         */
        private Map<String, String> chineseEnglish = new HashMap<>();

        /**
         * 映射的字段
         * key-字典值的字段
         * value-数据转换后的字段
         */
        private Map<String, String> mapping = new HashMap<>();

        /**
         * 属性的别名
         * key-属性名
         * value-别名
         */
        private Map<String, String> alias = new HashMap<>();

        /**
         * 创建数据转换处理模型
         * @param dataClass 数据模型
         * @param process 数据转换处理模型
         * @return DataChangeUtils.Process
         * @author zhouhao
         * @since  2021/9/7 9:55
         */
        private static Process create(Class<?> dataClass, Process process){
            if(!dataClass.isAnnotationPresent(ChangeModel.class)){
                throw new ChangeModelException("Data change model cannot be null!");
            }
            process.setChangeModel(dataClass.getAnnotation(ChangeModel.class));

            // 数据转换来源：数据代码模型
            if (process.getChangeModel().source().equals(ChangeModel.Source.ENUM)) {
                process.setModelCode(process.getChangeModel().value());
                if (!process.getChangeModel().modelCode().equals(Enum.class)) {
                    process.setModelCode(process.getChangeModel().modelCode());
                }
                if(process.getModelCode().equals(Enum.class)){
                    throw new ChangeModelException("Failed to bind code model!");
                }
                if(!Arrays.asList(process.getModelCode().getInterfaces()).contains(BaseCode.class)){
                    throw new ChangeModelException(String.format("%s must be implements com.stars.datachange.model.code.BaseCode", process.getModelCode().getName()));
                }
            }

            // 数据转换来源：数据字典
            if(process.getChangeModel().source().equals(ChangeModel.Source.DB)) {
                // 数据模型名称
                String key = StringUtils.isEmpty(process.getChangeModel().modelName())
                        ? dataClass.getSimpleName().substring(0,1).toLowerCase().concat(dataClass.getSimpleName().substring(1))
                        : process.getChangeModel().modelName();
                // v1.7 因最开始key的使用逻辑已经定义。故：退而求其次——modelName="-1"，认为查询整个字典表
                if ("-1".equals(key)) {
                    key = null;
                }
                try{
                    process.setDictionaryResult(dataDictionary.dataDictionary(key));
                }catch (ChangeException e){
                    throw e;
                }catch (Exception e){
                    throw new ChangeModelException("Failed to bind data dictionary, please check configuration!");
                }
                if(CollectionUtils.isEmpty(process.getDictionaryResult())){
                    throw new ChangeModelPropertyException("Please add some data to the data dictionary and try again!");
                }
            }

            Field[] fields = dataClass.getDeclaredFields();
            // 需要兼容的注解
            Set<Class<? extends Annotation>> compatible = new HashSet<>();
            for(Field field : fields){
                if(!field.isAccessible()){
                    field.setAccessible(true);
                }

                // 默认扫描数据模型下所有属性的注解
                if(process.getChangeModel().compatible().length == 0) {
                    compatible.addAll(Arrays.stream(field.getDeclaredAnnotations())
                            .filter(o -> !o.annotationType().getName().equals(ChangeModelProperty.class.getName()))
                            .map(Annotation::annotationType)
                            .collect(Collectors.toSet()));
                }

                if (process.getChangeModel().quick() && !field.isAnnotationPresent(ChangeModelProperty.class)) {
                    continue;
                }

                if (!process.getChangeModel().quick() && !field.isAnnotationPresent(ChangeModelProperty.class)) {
                    process.getIgnoreFields().add(field.getName());
                    process.getSkipComparisonFields().add(field.getName());
                    continue;
                }

                String name = field.getName();

                // 获取字段上的注解值
                ChangeModelProperty anon = field.getAnnotation(ChangeModelProperty.class);
                if(anon.split()){
                    process.getSplitFields().add(field.getName());
                }
                if(anon.ignore()){
                    process.getIgnoreFields().add(field.getName());
                }
                if(anon.bitOperation()){
                    process.getBitOperationFields().add(field.getName());
                }
                if(anon.skipComparison()){
                    process.getSkipComparisonFields().add(field.getName());
                }
                if(StringUtils.isNotEmpty(anon.chineseIgnoreDelimiter())){
                    process.getChineseIgnoreDelimiter().put(name, anon.chineseIgnoreDelimiter());
                }
                if(StringUtils.isNotEmpty(anon.delimiter())){
                    process.getSplitDelimiter().put(name, anon.delimiter());
                }

                String chinese = StringUtils.isNotEmpty(anon.value()) ? anon.value() : anon.chinese();
                if(StringUtils.isNotEmpty(chinese)){
                    String delimiter = anon.chineseIgnoreDelimiter();
                    process.getChineseEnglish().put(name, StringUtils.isEmpty(delimiter) ? chinese : chinese.split(delimiter)[0]);
                }else{
                    process.getChineseEnglish().put(name, name);
                }

                if(StringUtils.isNotEmpty(anon.mapping())){
                    process.getMapping().put(name, anon.mapping());
                }

                if(StringUtils.isNotEmpty(anon.alias())){
                    process.getAlias().put(name, anon.alias());
                }
            }
            // 需要兼容的注解
            if(process.getChangeModel().compatible().length == 0) {
                process.setCompatible(compatible);
            }else{
                process.setCompatible(new HashSet<>(Arrays.stream(process.getChangeModel().compatible())
                                .filter(o -> !o.getName().equals(ChangeModelProperty.class.getName()))
                                .collect(Collectors.toSet())));
            }

            // 若父类是通用的，跳过处理阶段
            if(!dataClass.getSuperclass().equals(Object.class) && dataClass.getSuperclass().isAnnotationPresent(ChangeModel.class)){
                create(dataClass.getSuperclass(), process);
            }
            return process;
        }

        /**
         * 是否忽略数据转换
         * @param field 字段名
         * @return boolean
         * @author zhouhao
         * @since  2021/9/7 11:29
         */
        boolean isIgnore(String field) {
            Set<String> fields = this.getIgnoreFields();
            if(CollectionUtils.isEmpty(fields)){
                return false;
            }
            return fields.contains(field);
        }

        /**
         * 是否进行数据拆分
         * @param field 字段名
         * @return boolean
         * @author zhouhao
         * @since  2021/9/7 11:29
         */
        boolean isSplit(String field) {
            Set<String> fields = this.getSplitFields();
            if(CollectionUtils.isEmpty(fields)){
                return false;
            }
            return fields.contains(field);
        }

        /**
         * 是否进行位运算
         * @param field 字段名
         * @return boolean
         * @author zhouhao
         * @since  2021/9/7 11:29
         */
        boolean isBitOperation(String field) {
            Set<String> fields = this.getBitOperationFields();
            if(CollectionUtils.isEmpty(fields)){
                return false;
            }
            return fields.contains(field);
        }

        /**
         * 是否跳过数据对比
         * @param field 字段名
         * @return boolean
         * @author zhouhao
         * @since  2021/9/7 11:29
         */
        boolean isSkipComparison(String field) {
            Set<String> fields = this.getSkipComparisonFields();
            if(CollectionUtils.isEmpty(fields)){
                return false;
            }
            return fields.contains(field);
        }
    }
}
