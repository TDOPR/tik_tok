package com.haoliang.common.annotation;

import java.lang.annotation.*;

/**
 * @author Dominick Li
 * @description 防止重复提交
 **/
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmit {


    /**
     * 间隔时间(单位 毫秒)
     */
    int interval() default 3000;

    /**
     * 请求成功后是否立刻释放锁
     */
    boolean delete() default true;

}
