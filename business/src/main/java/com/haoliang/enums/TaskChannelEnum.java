package com.haoliang.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskChannelEnum {

    TIKTOK(1, "TIktok"),
    DOU_YING(2, "抖音"),
    ;

    private int code;

    private String name;

}
