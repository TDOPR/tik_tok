package com.haoliang.model.vo;

import com.haoliang.constant.TiktokConfig;
import com.haoliang.model.VipOrders;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author Dominick Li
 * @Description
 * @CreateTime 2023/7/21 17:06
 **/
@Data
public class ZeroOrderVO {

    /**
     * 收益上限总量
     */
    private String total;

    /**
     * 剩余收益余量
     */
    private String allowance;

    /**
     * 已获取
     */
    private String acquired;

    /**
     * 是否有效  true=有效 false=无效
     */
    private boolean valid;

    /**
     * 有效天数 30
     */
    private Integer validDay;

    public ZeroOrderVO(VipOrders vipOrders) {
        if (vipOrders != null) {
            this.total = vipOrders.getTotal().toPlainString();
            BigDecimal allowanceB = vipOrders.getAllowance().add(vipOrders.getFrozenAmount());
            this.allowance = allowanceB.toPlainString();
            this.acquired = vipOrders.getTotal().subtract(allowanceB).toPlainString();
            this.valid = vipOrders.getValid() == 1;
            //计算剩余天数
            LocalDateTime localDateTime = LocalDateTime.now();
            validDay = (int) Duration.between(vipOrders.getCreateTime(), localDateTime).toDays();
            validDay = TiktokConfig.ZERO_LEVEL_FLUSHED_SEARCH_END_DAY - validDay;
            validDay = valid && validDay == 0 ? 1 : validDay;
            //如果可用余额为零
            if (allowanceB.compareTo(BigDecimal.ZERO) == 0) {
                this.valid = false;
            }
        }
    }

}
