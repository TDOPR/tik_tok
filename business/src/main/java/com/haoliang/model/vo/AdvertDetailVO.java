package com.haoliang.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author Dominick Li
 * @Description
 * @CreateTime 2023/6/16 10:22
 **/
@Data
public class AdvertDetailVO {

    /**
     * 描述
     */
    private String description;

    /**
     * 视频下载地址
     */
    private String video;

    /**
     * 视频的封面图
     */
    private String img;

    /**
     * 任务类型
     */
    private Integer type;

    /**
     * 渠道 1=Tiktok  2=抖音
     */
    private Integer channel;

}
