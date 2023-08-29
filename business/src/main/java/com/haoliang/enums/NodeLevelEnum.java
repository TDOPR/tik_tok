package com.haoliang.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public enum NodeLevelEnum {

    ZERO(0, "普通节点"),
    COMMUNITY(1, "社区节点", 1000, 1, 100000, BigDecimal.ZERO, new BigDecimal("0.2"), new BigDecimal("0.3"), 790, 4),
    SHAREHOLDER(2, "股东节点", 10000, 3, 1000000, new BigDecimal("0.1"), new BigDecimal("0.2"), new BigDecimal("0.3"), 21, 7);

    NodeLevelEnum(int level, String name) {
        this.level = level;
        this.name = name;
    }

    /**
     * 节点类型
     */
    private Integer level;

    /**
     * 节点名称
     */
    private String name;

    /**
     * 购买金额
     */
    private int amount;

    /**
     * 购买节点获得的社区等级
     */
    private int communityLevel;

    /**
     * 赠送的T币
     */
    private int giveTttAmount;

    /**
     * 社区新增业绩分红奖励 （每月2号结算获取上个月社区新增的购买vip消费）
     */
    private BigDecimal communityDividends;

    /**
     * 直推用户购买节点分红    只能是同级或比自己节点级别小的
     */
    private BigDecimal buyNodeDividends;

    /**
     * 直推用户购买任务次数包 获取的收益比例
     */
    private BigDecimal buyCountPackDividends;

    /**
     * 人数限制
     */
    private int limitSize;

    /**
     * 文本数量
     */
    private int textSize;

    public static NodeLevelEnum getByLevel(Integer level) {
        for (NodeLevelEnum nodeLevelEnum : values()) {
            if (nodeLevelEnum.getLevel().equals(level)) {
                return nodeLevelEnum;
            }
        }
        return null;
    }
}
