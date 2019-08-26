package com.seesea.redislock.annotations;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author xiechongyang
 * @description 定义分布式锁的注解
 * @createTime 2019/4/28 下午 2:46
 * @since JDK1.8
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DistributeLock {

    /**
     * 锁的资源 key
     * 支持 spring el 表达式
     *
     * @return
     */
    @AliasFor("key")
    String name();

    /**
     * 锁的资源 value
     * 支持spring el表达式
     *
     * @return
     */
    @AliasFor("value")
    String value();

    /**
     * 持锁时间，单位毫秒 默认一分钟
     *
     * @return
     */
    long keepMills() default 30000;

    /**
     * 当获取失败时候的操作
     *
     * @return
     */
    LockFailAction action() default LockFailAction.CONTINUE;

    /**
     * 重试的间隔时间，设置GIVEUP忽略此项  默认一秒重试
     */
    long sleepMills() default 1000;

    /**
     * 重试次数
     */
    int retryTimes() default 5;


    public enum LockFailAction {
        /**
         * 放弃
         */
        GIVEUP,
        /**
         * 继续
         */
        CONTINUE;
    }

}
