package com.haoliang.model.condition;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.haoliang.common.base.BaseCondition;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Dominick Li
 * @Description 审核列表使用的查询条件
 * @CreateTime 2023/3/1 15:53
 **/
@Data
public class CheckTaskCondition extends BaseCondition<LocalDateTime> {
    /**
     * 用户Id
     */
    private Integer userId;
    /**
     * 用户名
     */
    private String email;
    /**
     * tiktok账号
     */
    private String username;
    /**
     * 任务类型  1=关注任务 4=图片  5=广告
     */
    private Integer type;
    /**
     * 审核状态   2=待审核  3=已驳回 4=已完成
     */
    private Integer status;

    /**
     *渠道 1=Tiktok 2=抖音
     */
    private Integer channel;


    @Override
    public QueryWrapper buildQueryParam() {
        return null;
    }
}
