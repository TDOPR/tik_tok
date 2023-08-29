package com.haoliang.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author Dominick Li
 * @Description
 * @CreateTime 2023/3/27 14:17
 **/
@Data
@Builder
@AllArgsConstructor
public class MyCommunityAdminVO {


    /**
     * 总用户
     */
    private Integer allUser;

    /**
     * 有效用户
     */
    private Integer meshUser;

    /**
     * 零撸用户
     */
    private Integer zeroUser;

    /**
     * 社区用户
     */
    private Integer starUser;

    /**
     * 团队业绩
     */
    private String rechargeAmount;

    /**
     * 团队提现
     */
    private String WithdrawalAmount;

    /**
     * 团队总收益
     */
    private String total;

    /**
     * 团队总静态收益
     */
    private String totalStatic;

    /**
     * 团队总动态收益
     */
    private String totalDynamic;


}
