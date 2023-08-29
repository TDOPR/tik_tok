package com.haoliang.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Dominick Li
 * @Description
 * @CreateTime 2023/3/5 12:05
 **/
@Data
@Builder
@AllArgsConstructor
public class NodeLevelVO implements Comparable<NodeLevelVO> {

    /**
     * 节点名称
     */
    private String name;

    /**
     * 节点级别  1=社区节点  2=股东节点
     */
    private int level;

    /**
     * 限定名额
     */
    private long count;

    /**
     * 购买金额
     */
    private int amount;

    /**
     * 支付金额
     */
    private int payAmount;

    /**
     * 已购买的套餐
     */
    private boolean has;

    /**
     * 文本
     */
    private String text;

    /**
     * 显示的套餐文本信息
     */
    private List<String> textList;

    @Override
    public int compareTo(@NotNull NodeLevelVO other) {
        if      (this.level < other.level) return -1;
        else if (this.level > other.level) return +1;
        else                              return  0;
    }
}
