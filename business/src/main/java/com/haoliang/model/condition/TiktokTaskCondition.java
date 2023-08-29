package com.haoliang.model.condition;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.haoliang.common.base.BaseCondition;
import com.haoliang.enums.TaskTypeEnum;
import com.haoliang.model.TiktokTask;
import jodd.util.StringUtil;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * @author Dominick Li
 * @Description 查询还可以接单任务条件
 * @CreateTime 2023/2/24 14:22
 **/
@Data
public class TiktokTaskCondition extends BaseCondition<LocalDateTime> {

    /**
     * tiktok用户Id
     */
    private String userId;

    /**
     * tiktok用户名
     */
    private String userName;

    /**
     * 任务类型
     */
    private Integer type;

    /**
     * 渠道
     */
    private Integer channel;

    @Override
    public QueryWrapper buildQueryParam() {
        this.buildBaseQueryWrapper();
        this.getQueryWrapper().isNull("userId");
        this.getQueryWrapper().in("type", TaskTypeEnum.getNewTypeList());
        if (StringUtil.isNotBlank(userId)) {
            this.getQueryWrapper().eq("tiktokUserId", userId);
        }
        if (channel != null) {
            this.getQueryWrapper().eq("channel", channel);
        }
        if (type != null) {
            this.getQueryWrapper().eq("type", type);
        }
        if (StringUtil.isNotBlank(userName)) {
            this.getQueryWrapper().eq("userName", userName);
        }
        return this.getQueryWrapper();
    }
}
