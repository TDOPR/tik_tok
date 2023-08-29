package com.haoliang.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;


/**
 * @author Dominick Li
 * @Description
 * @CreateTime 2023/4/3 13:31
 **/
@Data
@Builder
public class FundVO {

    /**
     * 统计时间
     */
    private LocalDateTime now;

    /**
     * TVL
     */
    private String tvl;

    /**
     * 总入金
     */
    private String input;

    /**
     * 总出金
     */
    private String output;

    /**
     * 今日入金
     */
    private String yesterdayInput;

    /**
     * 今日出金
     */
    private String yesterdayOutput;
}
