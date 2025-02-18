package com.stars.datachange.module;

import com.stars.datachange.annotation.ChangeModel;
import com.stars.datachange.annotation.ChangeModelProperty;
import com.stars.datachange.annotation.ReentrantChangeModelProperty;
import com.stars.datachange.autoconfigure.StarsProperties;
import com.stars.datachange.exception.ChangeException;
import com.stars.datachange.exception.ChangeModelException;
import com.stars.datachange.exception.ChangeModelPropertyException;
import com.stars.datachange.model.code.BaseCode;
import com.stars.datachange.model.response.DataDictionaryResult;
import com.stars.datachange.utils.RegexUtils;
import com.stars.datachange.utils.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static com.stars.datachange.utils.DataChangeUtils.ROLLBACK;

/**
 * 数据转换处理模型
 * @author zhouhao
 * @since  2021/9/6 17:05
 */
@Slf4j
@Data
public class Process {

    /** 默认映射属性的后缀 */
    public static final String[] MAPPING_SUFFIX = {"Text", "Str", "Ext"};

    /** 数据转换模型 */
    private ChangeModel changeModel;

    /** 数据代码模型 */
    private Class<? extends Enum> modelCode;

    /** 数据转换源 */
    private ChangeModel.Source source;

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
     * 重入的字段
     */
    private Set<String> reentrantFields = new HashSet<>();

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
    private final Map<String, String> alias = new HashMap<>();

    /**
     * 创建数据转换处理模型
     * @param dataClass 数据模型
     * @param process 数据转换处理模型
     * @param dataDictionary 数据字典
     * @return DataChangeUtils.Process
     * @author zhouhao
     * @since  2021/9/7 9:55
     */
    public static Process create(Class<?> dataClass, Process process, DataDictionary dataDictionary){
        if(!dataClass.isAnnotationPresent(ChangeModel.class)){
            throw new ChangeModelException("Data change model cannot be null!");
        }

        process.setChangeModel(dataClass.getAnnotation(ChangeModel.class));
        process.setSource(process.getChangeModel().source());

        // 数据转换来源：自动
        if (process.getSource().equals(ChangeModel.Source.AUTO)) {
            if (!process.getChangeModel().value().equals(Enum.class) || !process.getChangeModel().modelCode().equals(Enum.class)) {
                process.setSource(ChangeModel.Source.ENUM);
            } else if (StringUtils.isNotEmpty(process.getChangeModel().modelName())) {
                process.setSource(ChangeModel.Source.DB);
            } else {
                process.setSource(ChangeModel.Source.NONE);
            }
        }

        // 数据转换来源：数据代码模型
        if (process.getSource().equals(ChangeModel.Source.ENUM)) {
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
        if(process.getSource().equals(ChangeModel.Source.DB)) {
            try{
                process.setDictionaryResult(dataDictionary.dataDictionary(process.getChangeModel().modelName()));
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

            // 可重入字段
            if (field.isAnnotationPresent(ReentrantChangeModelProperty.class)) {
                process.getReentrantFields().add(field.getName());
                continue;
            }

            // 数据转换源为空，跳过其他处理过程
            if(process.getSource().equals(ChangeModel.Source.NONE)) {
                continue;
            }

            // 默认扫描数据模型下所有属性的注解
            if(process.getChangeModel().compatible().length == 0) {
                compatible.addAll(Arrays.stream(field.getDeclaredAnnotations())
                        .map(Annotation::annotationType)
                        .filter(aClass -> !aClass.getName().equals(ChangeModelProperty.class.getName()))
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
            create(dataClass.getSuperclass(), process, dataDictionary);
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
    public boolean isIgnore(String field) {
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
    public boolean isSplit(String field) {
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
    public boolean isBitOperation(String field) {
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
    public boolean isSkipComparison(String field) {
        Set<String> fields = this.getSkipComparisonFields();
        if(CollectionUtils.isEmpty(fields)){
            return false;
        }
        return fields.contains(field);
    }

    /**
     * 是否可重入
     * @param field 字段名
     * @return boolean
     * @author zhouhao
     * @since  2021/9/7 11:29
     */
    public boolean isReentrant(String field) {
        Set<String> fields = this.getReentrantFields();
        if(CollectionUtils.isEmpty(fields)){
            return false;
        }
        return fields.contains(field);
    }

    /**
     * 多选值分割转义
     * @param modelCode 代码模型
     * @param name 要转义的字段名
     * @param data 多选值（逗号分割）
     * @return java.lang.String 转义后的多选值（逗号分割）
     * @author zhouhao
     * @since  2020/5/29 15:01
     */
    public static String splitConversion(Class<? extends Enum> modelCode, String name, String data) {
        return splitConversion(modelCode, name, data, ",");
    }

    /**
     * 多选值分割转义
     * @param result 数据字典结果集
     * @param key 要转义的字段名
     * @param data 多选值（逗号分割）
     * @return java.lang.String 转义后的多选值（逗号分割）
     * @author zhouhao
     * @since  2020/5/29 15:01
     */
    public static String splitConversion(Set<DataDictionaryResult> result, String key, String data) {
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
    public static String splitConversion(Class<? extends Enum> modelCode, String name, String data, String delimiter) {
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
    public static String splitConversion(Set<DataDictionaryResult> result, String key, String data, String delimiter) {
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
    public static String getValue(Set<DataDictionaryResult> result, String name, String oo) {
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
     * @since  2020/5/28 19:58
     */
    public static String bitOperation(Object o){
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
     * @since 2022/4/30 11:50
     * @param process 数据转换处理模型
     * @param dataClass 数据模型
     * @param field 源字段
     * @return java.lang.reflect.Field
     */
    public static Field getMappedField(Process process, Class<?> dataClass, Field field) {
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
     * @since 2022/4/30 11:50
     * @param dataClass 数据模型
     * @param list 字段列表
     * @return java.lang.reflect.Field
     */
    public static List<Field> getFields(Class<?> dataClass, List<Field> list) {
        list.addAll(new ArrayList<>(Arrays.asList(dataClass.getDeclaredFields())));
        // 若父类是通用的，跳过处理阶段
        if(!dataClass.getSuperclass().equals(Object.class) && dataClass.getSuperclass().isAnnotationPresent(ChangeModel.class)){
            getFields(dataClass.getSuperclass(), list);
        }
        return list;
    }
}
