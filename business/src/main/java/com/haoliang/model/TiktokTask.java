package com.haoliang.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.haoliang.common.base.BaseModel;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Dominick Li
 * @Description 公会中心发布的tiktok任务
 * @CreateTime 2023/2/24 11:28
 **/
@Data
@TableName("tiktok_task")
public class TiktokTask extends BaseModel {

    /**
     * tiktok用户名 广告任务描述
     */
    private String username;

    /**
     * 广告任务文件存储路径
     */
    private String opusId;

    /**
     * 发布任务的B端用户id
     */
    @JsonIgnore
    private Integer userId;

    /**
     * tiktok用户Id
     */
    @JsonProperty("userId")
    private String tiktokUserId;

    /**
     * 任务的数量
     */
    @NotNull
    private Integer num;

    /**
     * 剩余的数量
     */
    private Integer hasNum;

    /**
     * 任务类型 1=关注  2=点赞  3=评论   4=图片广告  5=视频广告
     */
    @NotNull
    private Integer type;

    /**
     * 渠道 1=tiktok  2=抖音
     */
    private Integer channel;

    /**
     * 是否系统内置任务 1=内置官方任务 0=非内置
     */
    private Integer built;

}
