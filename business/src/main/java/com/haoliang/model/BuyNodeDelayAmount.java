package com.haoliang.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.haoliang.common.base.BaseModelCID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Dominick Li
 * @Description 购买节点延迟奖励发放
 * @CreateTime 2023/7/27 16:04
 **/
@Data
@TableName("buy_node_delay_amount")
@Builder
@AllArgsConstructor
public class BuyNodeDelayAmount extends BaseModelCID {

    /**
     * 奖励获取的用户Id
     */
    private Integer userId;

    /**
     * 类型 1=购买节点奖励 2=推广购买节点奖励
     */
    private Integer type;

    /**
     * 已发放天数
     */
    private Integer day;

    /**
     * 推广的用户Id
     */
    private Integer subUserId;

    /**
     * 发放的总金额 每天发放的金额=总金额/200
     */
    private Integer amount;

}
