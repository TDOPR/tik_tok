package com.haoliang.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskPricesEnum {

    CONCERN(1,"关注"),
    ADVERT(2,"广告");

    private int type;

    private String desc;
}
