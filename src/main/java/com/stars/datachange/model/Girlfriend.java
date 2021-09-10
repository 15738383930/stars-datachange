package com.stars.datachange.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.stars.datachange.annotation.ChangeModel;
import com.stars.datachange.annotation.ChangeModelProperty;
import com.stars.datachange.model.code.GirlfriendCode;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * 女朋友
 * @author zhou
 * @date 2021/9/9 11:46
 */
@Data
@ChangeModel(source = ChangeModel.Source.DB)
public class Girlfriend {

    @ChangeModelProperty(value = "姓名", skipComparison = true)
    private String name;

    @ChangeModelProperty("类型： 1-安静 2-火辣 3-清爽")
    private Integer type;

    @ChangeModelProperty("交往时间")
    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM")
    private Date socialTime;

    @ChangeModelProperty(value = "喜欢的食物：\n" +
            "1-牛奶 \n" +
            "2-香蕉 \n" +
            "3-香肠 \n" +
            "4-黄瓜 \n" +
            "5-火锅", split = true)
    private String favoriteFood;

    @ChangeModelProperty(value = "照片", ignore = true)
    private List<String> photo;

}
