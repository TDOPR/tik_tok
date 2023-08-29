package com.haoliang.model.dto;

import lombok.Data;

/**
 * @author Dominick Li
 * @Description
 * @CreateTime 2023/6/19 16:43
 **/
@Data
public class BuyPackageDTO {

    /**
     * 购买的vip套餐 id
     */
    private Integer id;

    /**
     * 抵扣的ttt金额
     */
    private Integer tttAmount=0;

}
