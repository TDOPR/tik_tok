package com.haoliang.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Dominick Li
 * @Description
 * @CreateTime 2023/2/27 17:19
 **/
@Data
public class PublishConcernTiktokTaskDTO {

    /**
     * tiktok用户名
     */
    private String username;

    /**
     * tiktok用户Id
     */
    private String tiktokUserId;

    /**
     * 数量
     */
    @NotNull
    private Integer num;

    /**
     * 渠道 1=tiktok  0=抖音
     */
    @NotNull
    private Integer channel;
}
