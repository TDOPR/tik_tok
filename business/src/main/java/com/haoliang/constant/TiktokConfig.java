package com.haoliang.constant;


import java.math.BigDecimal;

/**
 * @author Dominick Li
 * @Description 业务场景类
 * @CreateTime 2022/11/1 11:09
 **/
public class TiktokConfig {

    /**
     * TTT账单明细里 代理收益的国际化信息Key
     */
    public static final String AGENCY_KEY = "ttt.0";
    /**
     * TTT账单明细 任务收益的国际化信息Key
     */
    public static final String TASK_KEY = "ttt.1";
    /**
     * 特别奖分红比例
     */
    public static final BigDecimal SPECIAL_AWARD_RATE = new BigDecimal("0.03");
    /**
     * 零撸用户直接推荐3位有效用户即可提取收益
     */
    public static final int MIN_USER_COUNT = 3;

    /**
     * v1 ttt抵扣金额
     */
    public static final BigDecimal V1_USD = new BigDecimal("15");
    /**
     * v2 ttt抵扣金额
     */
    public static final BigDecimal V2_USD = new BigDecimal("30");
    /**
     * usd 位数 保留2位usd
     */
    public static final Integer NUMBER_OF_DIGITS = 2;

    /**
     * 零撸用户的套餐刷新查询结束时间 30天前
     */
    public static final int ZERO_LEVEL_FLUSHED_SEARCH_END_DAY = 30;

    public static final Integer NEWS_TYPE = 1;

    public static final Integer NOTICE_TYPE = 2;

    public static final String ID_PAY_NAME = "guild";

    /**
     * 数字货币提现限制
     */
    public static final Integer WITHDRAW_COUNT_LIMIT = 3;

    /**
     * 最小充值USD金额
     */
    public static final BigDecimal RECHARGE_MIN_LIMIT = new BigDecimal("10");

    /**
     * 更新用户代理商等级的定时任务名称(用于分布式锁)
     */
    public final static String UPDATE_USER_LEVEL_TASK_METHOD_NAME = "updateUserLevelTask";

    /**
     * 零撸超时时间 60天
     */
    public final static Integer ZERO_EXPIRED_DAY =60;

    /**
     * 每天可以接的广告数限制
     */
    public final static Integer ADVERT_LIMIT=3;

    /**
     * 每个邮箱每天收件上限
     */
    public final static Integer SEND_EMAIL_LIMIT_SIZE=5;

    /**
     * 提现最低金额
     */
    public final static BigDecimal WITHDRAW_MIN_AMOUNT =new BigDecimal(15);

    /**
     * 购买节点延迟发放的天数
     */
    public final static int DELAY_SEND_DAY=200;

}
