package com.stars.datachange.utils;

import com.stars.datachange.annotation.ChangeModel;
import com.stars.datachange.annotation.ChangeModelProperty;
import com.stars.datachange.autoconfigure.StarsProperties;
import com.stars.datachange.exception.ChangeModelException;
import com.stars.datachange.exception.ChangeModelPropertyException;
import com.stars.datachange.mapper.StarsDictionaryMapper;
import com.stars.datachange.model.response.DataChangeContrastResult;
import com.stars.datachange.model.response.DataDictionaryResult;
import com.stars.datachange.module.Compatible;
import com.stars.datachange.module.DefaultCompatible;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据转换工具类
 * @author zhou
 * @since 2021/9/6 10:00
 */
@Component
public final class DataChangeUtils {

    private static StarsDictionaryMapper starsDictionaryMapper;

    private static Compatible compatible;

    public DataChangeUtils(StarsDictionaryMapper starsDictionaryMapper, Compatible compatible){
        DataChangeUtils.starsDictionaryMapper = starsDictionaryMapper;
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
     * @throws java.lang.Exception 异常
     */
    public static Map<String, Object> dataChange(Object data) throws Exception {
        return dataChange(data, null);
    }

    private static Map<String, Object> dataChange(Object data, Process process) throws Exception {
        final Class<?> dataClass = data.getClass();

        Map<String, Object> result = BeanUtils.beanToMap(data);
        if(Objects.isNull(result)) {
            return new HashMap<>();
        }

        if (Objects.isNull(compatible)) {
            compatible = DefaultCompatible.get();
        }
        compatible.run(dataClass, result);

        if(Objects.isNull(process)){
            process = Process.create(dataClass, new Process());
        }

        for (String key : result.keySet()) {
            if(Objects.isNull(result.get(key))){
                continue;
            }

            if(process.isIgnore(key)){
                continue;
            }

            // 位运算转换
            if(process.isBitOperation(key)){
                if (process.getChangeModel().source().equals(ChangeModel.Source.ENUM)) {
                    result.put(key, splitConversion(process.getModelCode(), key, bitOperation(Integer.parseInt(result.get(key).toString()))));
                }
                if (process.getChangeModel().source().equals(ChangeModel.Source.DB)) {
                    result.put(key, splitConversion(process.getDictionaryResult(), key, bitOperation(Integer.parseInt(result.get(key).toString()))));
                }
                continue;
            }

            // 分割转换
            if(process.isSplit(key)){
                if (process.getChangeModel().source().equals(ChangeModel.Source.ENUM)) {
                    result.put(key, splitConversion(process.getModelCode(), key, result.get(key).toString(), process.getSplitDelimiter().get(key)));
                }
                if (process.getChangeModel().source().equals(ChangeModel.Source.DB)) {
                    result.put(key, splitConversion(process.getDictionaryResult(), key, result.get(key).toString(), process.getSplitDelimiter().get(key)));
                }
                continue;
            }

            // 转换
            if (process.getChangeModel().source().equals(ChangeModel.Source.ENUM)) {
                result.put(key, getValue(process.getModelCode(), key, result.get(key).toString()));
                continue;
            }
            result.put(key, getValue(process.getDictionaryResult(), key, result.get(key).toString()));
        }
        return result;
    }

    /**
     * 数据转换对比
     * <br>
     * <br>调用此方法，可以找出两个对象的差异，并收集差异
     * @param oldData 变更前的数据
     * @param newData 变更后的数据
     * @return 数据转换对比后的结果集
     * @throws java.lang.Exception 异常
     */
    public static List<DataChangeContrastResult> dataContrast(Object oldData, Object newData) throws Exception {
        List<DataChangeContrastResult> result = new ArrayList<>();

        final Process process = Process.create(oldData.getClass(), new Process());

        if (process.getChangeModel().quick()) {
            throw new ChangeModelException("This function does not support shortcut mode temporarily!");
        }

        Map<String, Object> oldData_ = dataChange(oldData, process);
        Map<String, Object> newData_ = dataChange(newData, process);

        oldData_.keySet().forEach(key -> {
            if(process.isSkipComparison(key)) {
                return;
            }
            Object old_ = oldData_.get(key);
            Object new_ = newData_.get(key);
            if(Objects.nonNull(old_)){
                if(!old_.equals(new_)){
                    result.add(DataChangeContrastResult.builder().name(process.getChineseEnglish().get(key)).oldData(old_).newData(new_).build());
                }
            }else if(Objects.nonNull(new_)){
                result.add(DataChangeContrastResult.builder().name(process.getChineseEnglish().get(key)).oldData(null).newData(new_).build());
            }
        });
        return result;
    }

    /**
     * 多选值分割转义
     * @param key 要转义的字段名
     * @param data 多选值（逗号分割）
     * @return java.lang.String 转义后的多选值（逗号分割）
     * @author zhouhao
     * @since  2020/5/29 15:01
     */
    private static String splitConversion(Class<? extends Enum> modelCode, String key, String data) {
        return splitConversion(modelCode, key, data, ",");
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
     * @param key 要转义的字段名
     * @param data 多选值（regex分割）
     * @param delimiter 分割符
     * @return java.lang.String 通过delimiter转义后的多选值
     * @author zhouhao
     * @since  2020/5/29 15:01
     */
    private static String splitConversion(Class<? extends Enum> modelCode, String key, String data, String delimiter) {
        if(StringUtils.isEmpty(delimiter)){
            return splitConversion(modelCode, key, data);
        }
        if (StringUtils.isNotEmpty(data)) {
            String delimiter_ = delimiter.replace(".", "\\.").replace("|", "\\|");
            List<String> list = Arrays.asList(data.split(delimiter_));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                String s = list.get(i);
                s = (String) getValue(modelCode, key, s);
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
                String s = list.get(i);
                s = getValue(result, key, s);
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
     * @param modelCode 代码模型
     * @param key 属性名
     * @param value 属性代码
     * @return java.lang.Object 转义后的值
     * @author zhouhao
     * @since  2020/5/29 13:16
     */
    private static Object getValue(Class<? extends Enum> modelCode, String key, String value) {
        try {
            return modelCode.getMethod("getValue", String.class, String.class).invoke(modelCode, key, value);
        } catch (Exception e) {
            throw new ChangeModelPropertyException("Data conversion failed, please check your dictionary enumeration configuration!");
        }
    }

    /**
     * 获取属性值
     * @param result 数据字典
     * @param name 属性名
     * @param code 属性代码
     * @return java.lang.Object 转义后的值
     * @author zhouhao
     * @since  2020/5/29 13:16
     */
    private static String getValue(Set<DataDictionaryResult> result, String name, String code) {
        List<DataDictionaryResult> collect = result.stream().filter(o -> o.getName().equals(name)).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(collect)){
            return code;
        }
        List<DataDictionaryResult.Map> maps = collect.get(0).getMaps().stream().filter(o -> o.getCode().equals(code)).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(maps)){
            return code;
        }
        return maps.get(0).getValue();
    }

    /**
     * 位运算
     * <br>
     * <br>实现思路：
     *      <br>把位运算的数值转换为二进制码，把二进制码反转方便后面遍历计算。
     *      <br>遍历二级制码时，遇1计算当前指针的平方，即：得出的多选值code，并添加到多选值列表中
     *      <br>遍历结束后，按照多选值的大小进行排序并以逗号分隔的形式返回
     * @param num 位数值
     * @return java.lang.String 运算后的多选值（逗号分隔）
     * @author zhouhao
     * @date  2020/5/28 19:58
     */
    private static String bitOperation(int num){
        String data = new StringBuffer(Integer.toBinaryString(num)).reverse().toString();
        List<Integer> ints = new ArrayList<>();
        int t = data.length();
        for (int i = t - 1; i >= 0; i--) {
            t = data.lastIndexOf("1",t - 1);
            if(t == -1){
                break;
            }
            ints.add((int) Math.pow(2, t));
        }
        return ints.stream().sorted().map(Object::toString).collect(Collectors.joining(","));
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
         * 创建数据转换处理模型
         * @param dataClass 数据模型
         * @param process 数据转换处理模型
         * @return com.ucan.shweilao.utils.DataChangeUtils.Process
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
            }

            // 数据转换来源：数据字典
            if(process.getChangeModel().source().equals(ChangeModel.Source.DB)) {
                // 数据模型名称
                String key = process.getChangeModel().modelName();
                if(StringUtils.isEmpty(key)){
                    key = dataClass.getSimpleName().substring(0,1).toLowerCase().concat(dataClass.getSimpleName().substring(1));
                }
                try{
                    process.setDictionaryResult(starsDictionaryMapper.findList(StarsProperties.dictionary, key));
                }catch (Exception e){
                    throw new ChangeModelException("Failed to bind data dictionary, please check configuration!");
                }
                if(CollectionUtils.isEmpty(process.getDictionaryResult())){
                    throw new ChangeModelPropertyException("Please add some data to the data dictionary and try again!");
                }
            }

            Field[] fields = dataClass.getDeclaredFields();
            for(Field field : fields){
                if(!field.isAccessible()){
                    field.setAccessible(true);
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
            }
            if(!dataClass.getSuperclass().equals(Object.class)){
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
