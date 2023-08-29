package com.haoliang.enums;


import com.haoliang.common.model.ThreadLocalManager;
import com.haoliang.common.util.MessageUtil;
import com.haoliang.constant.TiktokConfig;

import java.util.Arrays;
import java.util.List;

/**
 * @author Dominick Li
 * @Description 钱包流水类型枚举
 * @CreateTime 2022/11/1 10:30
 **/
public enum TttLogTypeEnum {

    ALGEBRA(1, "代数奖励"),
    TEAM(2, "团队奖励"),
    SPECIAL(3, "分红奖励"),
    HOLDING_COINS(4, "持币奖励"),

    LIKE(5, "点赞"),
    CONCERN(6, "关注"),
    COMMENTS(7, "评论"),

    TO_USD(8, "转出到USD账户"),
    ENROLL_IN_BENEFITS(9, "注册福利"),
    BUY_VIP(10, "抵扣购买VIP"),
    EXPIRED(11, "已过期"),

    IMG_ADVERT(12, "图片"),
    VIDEO_ADVERT(13, "视频"),
    BUY_TASK_NUM_PACKAGE(14, "抵扣购买投放次数"),
    POPULARIZE_ADVERT(15, "推广广告投放奖励"),
    BUY_NODE_LEVEL_INPUT(16, "购买节点奖励"),
    RECOMMEND_BUY_NODE_LEVEL(17, "推广购买节点奖励"),
    PERFORMANCE_BONUS(18, "业绩分红奖励");

    private Integer value;

    private String name;

    private String key;

    public String getKey() {
        return key;
    }

    public Integer getValue() {
        return value;
    }

    /**
     * 国际化信息文件里的Key前缀
     */
    private final static String prefix = "tttLogType.";

    TttLogTypeEnum(Integer value, String name) {
        this.value = value;
        this.name = name;
        this.key = prefix + value;
    }

    public static String getDescByValue(Integer value) {
        for (TttLogTypeEnum tttLogTypeEnum : values()) {
            if (tttLogTypeEnum.getValue().equals(value)) {
                return tttLogTypeEnum.toString();
            }
        }
        return "";
    }

    public static String getTttDetailText(Integer value) {
        String key;
        if (value == 0) {
            key = TiktokConfig.AGENCY_KEY;
        } else if (value == 1) {
            key = TiktokConfig.TASK_KEY;
        } else {
            key = valueOf(value).getKey();
        }
        return MessageUtil.get(key, ThreadLocalManager.getLanguage());
    }

    public static TttLogTypeEnum valueOf(Integer value) {
        for (TttLogTypeEnum tttLogTypeEnum : values()) {
            if (tttLogTypeEnum.getValue().equals(value)) {
                return tttLogTypeEnum;
            }
        }
        return null;
    }

    /**
     * 动态收益类型
     */
    public static List<Integer> getDynamicTypeList() {
        return Arrays.asList(ALGEBRA.getValue(), TEAM.getValue(), SPECIAL.getValue(), HOLDING_COINS.getValue(), POPULARIZE_ADVERT.getValue(), RECOMMEND_BUY_NODE_LEVEL.getValue(), PERFORMANCE_BONUS.getValue());
    }

    /**
     * 任务收益类型
     */
    public static List<Integer> getTaskTypeList() {
        return Arrays.asList(LIKE.getValue(), CONCERN.getValue(), COMMENTS.getValue(), IMG_ADVERT.getValue(), VIDEO_ADVERT.getValue());
    }

    /**
     * 其它类型
     */
    public static List<Integer> getOtherTypeList() {
        return Arrays.asList(TO_USD.getValue(), ENROLL_IN_BENEFITS.getValue(), BUY_VIP.getValue(), EXPIRED.getValue(), BUY_TASK_NUM_PACKAGE.getValue(), BUY_NODE_LEVEL_INPUT.getValue());
    }

    @Override
    public String toString() {
        return MessageUtil.get(key, ThreadLocalManager.getLanguage());
    }

    /**
     * 根据任务类型获取流水类型
     */
    public static TttLogTypeEnum getTypeByTaskType(Integer type) {
        TaskTypeEnum taskTypeEnum = TaskTypeEnum.valueOf(type);
        switch (taskTypeEnum) {
            case CONCERN:
                return CONCERN;
            case COMMENTS:
                return COMMENTS;
            case LIKE_TASK:
                return LIKE;
            case IMG_ADVERT:
                return IMG_ADVERT;
            case VIDEO_ADVERT:
                return VIDEO_ADVERT;
        }
        return null;
    }

}
