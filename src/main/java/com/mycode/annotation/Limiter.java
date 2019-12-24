package com.mycode.annotation;

import java.lang.annotation.*;

/**
 * 自定义注解
 * api防刷限流
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Limiter {

    /**
     * 从第一次访问接口的时间到cycle时间周期内，访问次数不得超过frequency
     * 默认为20次
     * @return
     */
    int frequency() default 20;

    /**
     * 周期时间 单位为ms
     * 默认为一分钟
     * @return
     */
    int cycle() default 60*1000;

    /**
     * 返回的提示信息
     */
    String message() default "请求过于频繁";

    /**
     * 到期时间，单位为S
     * 如果在cecle时间周期内访问超过frequency次
     * 则默认1分钟内无法继续访问
     * @return
     */
    long expireTime() default  10;

}
