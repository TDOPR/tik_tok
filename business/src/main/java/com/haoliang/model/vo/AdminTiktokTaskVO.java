package com.haoliang.model.vo;

import com.haoliang.model.TiktokTask;
import lombok.Data;

/**
 * @author Dominick Li
 * @Description
 * @CreateTime 2023/5/6 12:11
 **/
@Data
public class AdminTiktokTaskVO extends TiktokTask {

    /**
     * 是否被用户接单标识 true=已接 false=未接
     */
    private boolean userUse;

    /**
     * 广告描述
     */
    private String desc;

    /**
     * 广告图片
     */
    private String img;

    /**
     * 广告视频
     */
    private String video;
}
