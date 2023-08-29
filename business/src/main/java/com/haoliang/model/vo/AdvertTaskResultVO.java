package com.haoliang.model.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Dominick Li
 * @Description
 * @CreateTime 2023/3/1 14:57
 **/
@Data
@NoArgsConstructor
public class AdvertTaskResultVO {

    /**
     * 剩余可完成收益
     */
    private String remainde;

    /**
     * 广告数
     */
    private Integer success;

    /**
     * 可完成总数
     */
    private Integer total;

    /**
     * 广告列表
     */
    private List<AdvertTaskVO> content;

    public AdvertTaskResultVO(String remainde,Integer success, Integer total, List<AdvertTaskVO> content) {
        this.remainde=remainde;
        this.success = success;
        this.total = total;
        this.content = content;
    }

}
