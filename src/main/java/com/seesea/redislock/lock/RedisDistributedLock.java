package com.seesea.redislock.lock;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author xiechongyang
 * @description
 * @createTime 2019/4/28 下午 3:37
 * @since JDK1.8
 */
public class RedisDistributedLock extends AbstractDistributedLockImpl {

    private static final Logger logger = LoggerFactory.getLogger(RedisDistributedLock.class);

    private RedisTemplate<String, Object> redisTemplate;

    private ThreadLocal<String> lockFlag = new ThreadLocal<>();

    private static final String UNLOCK_LUA;

    private static final String SET_IF_NOT_EXIST = "NX";

    private static final String SET_WITH_EXPIRE_TIME = "PX";


    static {
        UNLOCK_LUA = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    }

    public RedisDistributedLock(RedisTemplate<String, Object> redisTemplate) {
        super();
        logger.info("初始化分布式锁处理类");
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean lock(String key, long expire, int retryTimes, long sleepMillis) {
        boolean result = setRedis(key, expire);
        // 如果获取锁失败，按照传入的重试次数进行重试
        while ((!result) && retryTimes-- > 0) {
            try {
                System.out.println(key + "锁失败, 重试..." + retryTimes);
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
                System.out.println("延时异常");
                e.printStackTrace();
                return false;
            }
            result = setRedis(key, expire);
        }
        return result;
    }

    /**
     * 在获取锁的时候就能够保证设置 Redis 值和过期时间的原子性，避免前面提到的两次 Redis 操作期间出现意外而导致的锁不能释放的问题。但是这样还是可能会存在一个问题，考虑如下的场景顺序：
     * <p>
     * 1. 线程T1获取锁
     * 2. 线程T1执行业务操作，由于某些原因阻塞了较长时间
     * 3. 锁自动过期，即锁自动释放了
     * 4. 线程T2获取锁
     * 5. 线程T1业务操作完毕，释放锁（其实是释放的线程T2的锁）
     * 6. 按照这样的场景顺序，线程T2的业务操作实际上就没有锁提供保护机制了。所以，每个线程释放锁的时候只能释放自己的锁，即锁必须要有一个拥有者的标记，并且也需要保证释放锁的原子性操作。
     * <p>
     * 因此在获取锁的时候，可以生成一个随机不唯一的串放入当前线程中，然后再放入 Redis 。释放锁的时候先判断锁对应的值是否与线程中的值相同，相同时才做删除操作
     *
     * @param key redis key
     * @return 是否释放锁成功
     */
    @Override
    public boolean releaseLock(String key) {
        // 释放锁的时候，有可能因为持锁之后方法执行时间大于锁的有效期，此时有可能已经被另外一个线程持有锁，所以不能直接删除

        try {
            List<String> keys = new ArrayList<>();
            keys.add(key);
            List<String> args = new ArrayList<>();
            args.add(lockFlag.get());


            // 使用lua脚本删除redis中匹配value的key，可以避免由于方法执行时间过长而redis锁自动过期失效的时候误删其他线程的锁
            // spring自带的执行脚本方法中，集群模式直接抛出不支持执行脚本的异常，所以只能拿到原redis的connection来执行脚本
            Long result = redisTemplate.execute(new RedisCallback<Long>() {
                @Override
                public Long doInRedis(RedisConnection redisConnection) throws DataAccessException {
                    Object nativeConnection = redisConnection.getNativeConnection();
                    // 集群模式和单机模式虽然执行脚本的方法一样，但是没有共同的接口，所以只能分开执行
                    // 集群模式
                    if (nativeConnection instanceof JedisCluster) {
                        return (Long) ((JedisCluster) nativeConnection).eval(UNLOCK_LUA, keys, args);
                    }

                    // 单机模式
                    else if (nativeConnection instanceof Jedis) {
                        return (Long) ((Jedis) nativeConnection).eval(UNLOCK_LUA, keys, args);
                    }
                    return 0L;
                }
            });
            return result != 0L && result > 0;

        } catch (Exception e) {
            System.out.println("释放锁定发生异常");
            e.printStackTrace();
        } finally {
            // 清除掉ThreadLocal中的数据，避免内存溢出
            lockFlag.remove();
        }
        return false;
    }

    private boolean setRedis(String key, long expire) {
        try {
            String result = redisTemplate.execute((RedisCallback<String>) redisConnection -> {
                JedisCommands commands = (JedisCommands) redisConnection.getNativeConnection();
                String uuid = UUID.randomUUID().toString();
                lockFlag.set(uuid);
                return commands.set(key, uuid, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expire);
            });
            return !StringUtils.isEmpty(result);
        } catch (Exception e) {
            System.out.println("执行");
            e.printStackTrace();
        }
        return false;
    }


}
