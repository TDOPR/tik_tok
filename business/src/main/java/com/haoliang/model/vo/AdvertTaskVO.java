package com.haoliang.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;


/**
 * @author Dominick Li
 * @Description 广告任务
 * @CreateTime 2023/6/15 18:20
 **/
@Data
public class AdvertTaskVO {


    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 描述
     */
    private String description;

    /**
     * 封面图
     */
    private String img;

    /**
     * 收益金额
     */
    private String amount;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 渠道
     */
    private Integer channel;

    /**
     * 图片存储路径
     */
    @JsonIgnore
    private String opusId;

}
