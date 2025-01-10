package org.start.app.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁工具类
 */
@Component
public class RedisLockUtil {
    private static final Logger logger = LoggerFactory.getLogger(RedisLockUtil.class);
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    /** 默认锁的超时时间（秒） */
    private static final long DEFAULT_LOCK_TIMEOUT = 30;
    /** 默认获取锁的重试间隔（毫秒） */
    private static final long DEFAULT_RETRY_INTERVAL = 100;
    /** 默认获取锁的超时时间（秒） */
    private static final long DEFAULT_ACQUIRE_TIMEOUT = 3;

    public RedisLockUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取分布式锁并执行
     * @param lockKey 锁的key
     * @param action 要执行的操作
     * @return 操作结果
     */
    public <T> T executeWithLock(String lockKey, LockAction<T> action) {
        return executeWithLock(lockKey, DEFAULT_LOCK_TIMEOUT, DEFAULT_ACQUIRE_TIMEOUT, action);
    }

    /**
     * 获取分布式锁并执行（带超时时间）
     * @param lockKey 锁的key
     * @param lockTimeout 锁的超时时间（秒）
     * @param acquireTimeout 获取锁的超时时间（秒）
     * @param action 要执行的操作
     * @return 操作结果
     */
    public <T> T executeWithLock(String lockKey, long lockTimeout, long acquireTimeout, LockAction<T> action) {
        String finalLockKey = "lock:" + lockKey;
        long startTime = System.currentTimeMillis();
        boolean locked = false;

        try {
            // 尝试获取锁，直到超时
            while (!locked && System.currentTimeMillis() - startTime < acquireTimeout * 1000) {
                locked = redisTemplate.opsForValue().setIfAbsent(finalLockKey, Thread.currentThread().getId(), lockTimeout, TimeUnit.SECONDS);
                if (!locked) {
                    logger.debug("等待获取锁: {}", finalLockKey);
                    Thread.sleep(DEFAULT_RETRY_INTERVAL);
                }
            }

            if (!locked) {
                throw new RuntimeException("获取锁超时: " + finalLockKey);
            }

            logger.debug("成功获取锁: {}", finalLockKey);
            return action.execute();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取锁被中断: " + finalLockKey, e);
        } finally {
            if (locked) {
                redisTemplate.delete(finalLockKey);
                logger.debug("释放锁: {}", finalLockKey);
            }
        }
    }

    /**
     * 锁定操作接口
     */
    @FunctionalInterface
    public interface LockAction<T> {
        T execute();
    }
} 