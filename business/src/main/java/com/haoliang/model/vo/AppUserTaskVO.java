package com.haoliang.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.haoliang.common.util.NumberUtil;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Dominick Li
 * @Description
 * @CreateTime 2023/2/27 11:59
 **/
@Data
public class AppUserTaskVO {

    /**
     * 用户接取的任务Id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * tiktokName
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String username;

    /**
     * tiktok用户Id
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String tiktokUserId;

    /**
     * 作品Id
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String opusId;

    /**
     * 旧版  1=关注 2=点赞 3=评论
     * 类型  1=关注 2=图片广告 3=视频广告
     */
    private Integer  type;

    /**
     * 待审需要的截图文件存储路径
     */
    private String imgUrl;

    /**
     * 金额
     */
    private String amount;

    /**
     * 内置任务类型
     */
    private Integer built;

    /**
     * 渠道
     */
    private Integer channel;

    /**
     * 广告类型
     */
    @JsonIgnore
    private Integer advertType;

    /**
     * 金额
     */
    @JsonIgnore
    private BigDecimal dAmount;

    public String getAmount() {
        return NumberUtil.toPlainString(dAmount);
    }
}
