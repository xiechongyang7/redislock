package com.seesea.redislock.lock;

/**
 * @author xiechongyang
 * @description 分布式锁方法
 * @createTime 2019/4/28 下午 3:13
 * @since JDK1.8
 */
public interface IDistributedLock {

    static final long TIMEOUT_MILLIS = 5000;

    static final int RETRY_TIMES = Integer.MAX_VALUE;

    static final long SLEEP_MILLIS = 500;

    boolean lock(String key);

    boolean lock(String key, int retryTimes);

    boolean lock(String key, int retryTimes, long sleepMillis);

    boolean lock(String key, long expire);

    boolean lock(String key, long expire, int retryTimes);

    boolean lock(String key, long expire, int retryTimes, long sleepMillis);

    boolean releaseLock(String key);
}
