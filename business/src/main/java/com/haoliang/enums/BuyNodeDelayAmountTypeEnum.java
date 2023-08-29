package com.haoliang.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BuyNodeDelayAmountTypeEnum {

    BUY(1, "购买节点奖励"),
    PROMOTION(2, "推广购买节点奖励"),
    ;

    private int type;

    private String name;


    /**
     * 获取流水类型
     * @param type 根据节点江流类型
     * @return
     */
    public static Integer getTttLogTypeBy(Integer type) {
        return type == BUY.getType() ? TttLogTypeEnum.BUY_NODE_LEVEL_INPUT.getValue() : TttLogTypeEnum.RECOMMEND_BUY_NODE_LEVEL.getValue();
    }

}
