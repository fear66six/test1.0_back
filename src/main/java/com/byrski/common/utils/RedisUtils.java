package com.byrski.common.utils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
@Slf4j
public class RedisUtils {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    public Boolean exist(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, String value, long seconds) {
        stringRedisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
    }

    public Boolean delete(String key) {
        return delete(List.of(key)) == 1;
    }

    public Long delete(List<String> keys) {
        return stringRedisTemplate.delete(keys);
    }

    public Long incr(String key) {
        return incrBy(key, 1L);
    }

    public Long incrBy(String key, long increment) {
        return stringRedisTemplate.opsForValue().increment(key, increment);
    }

    public Boolean expire(String key, int expire) {
        return stringRedisTemplate.expire(key, expire, TimeUnit.SECONDS);
    }

    public boolean acquire(String key, final int expireTime) {
        try {
            String lockKey = key + ".lock";
            int timeout = 3000;
            while (timeout >= 0) {
                long expires = System.currentTimeMillis() + expireTime * 1000L + 1;
                String expiresStr = String.valueOf(expires); // 锁到期时间
                if (Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(lockKey, expiresStr))) {
                    return true;
                }

                String currentValueStr = stringRedisTemplate.opsForValue().get(lockKey); // redis里的时间
                if (currentValueStr != null && Long.parseLong(currentValueStr) < System.currentTimeMillis()) {
                    // 判断是否为空，不为空的情况下，如果被其他线程设置了值，则第二个条件判断是过不去的
                    // lock is expired
                    String oldValueStr = stringRedisTemplate.opsForValue().getAndSet(lockKey, expiresStr);
                    // 获取上一个锁到期时间，并设置现在的锁到期时间，
                    // 只有一个线程才能获取上一个线上的设置时间，因为jedis.getSet是同步的
                    if (oldValueStr != null && oldValueStr.equals(currentValueStr)) {
                        // 如过这个时候，多个线程恰好都到了这里，但是只有一个线程的设置值和当前值相同，他才有权利获取锁
                        return true;
                    }
                }
                timeout -= 100;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
            return false;
        } catch (Exception e) {
            log.error("exception occur when calling tryLock, key: " + key, e);
            return false;
        }
    }

    public boolean release(String key) {
        try {
            String lockKey = key + ".lock";
            return delete(lockKey);
        } catch (Exception e) {
            log.error("exception occur when calling unLock, key: " + key, e);
            return false;
        }
    }

    public <T> T lock(String key, int expireTime, LockCallback<T> lockCallback) throws Exception {
        boolean lock = false;
        try {
            lock = acquire(key, expireTime);
            if (!lock) {
                log.warn("lock limit,key is {}", key);
                return null;
            }
            T result = lockCallback.execute();
            return result;
        } catch (Exception e) {
            throw e;
        } finally {
            if (lock) {
                try {
                    boolean unLock = release(key);
                } catch (Exception e) {
                    log.error(" unlock error key is {}", key, e);
                    throw new Exception(e);
                }
            }
        }
    }

    public <T> T lock(String key, LockCallback<T> lockCallback) throws Exception {
        return lock(key, Const.DEFAULT_EXPIRE_TIME, lockCallback);
    }

    public boolean lockWithExpire(String key, final int expireTime) {
        try {
            String lockKey = key + ".lock";
            String ret = stringRedisTemplate.opsForValue().getAndSet(lockKey, lockKey);
            boolean notHaveVal = (ret == null);
            expire(lockKey, expireTime);
            return notHaveVal;
        } catch (Exception e) {
            log.error("exception occur when calling lockWithExpire, key: " + key, e);
            return false;
        }
    }

    public interface LockCallback<T> {
        T execute() throws Exception;
    }

    public abstract static class LockCallbackWithoutRet implements LockCallback<Object>{
        public final Object execute() throws Exception {
            executeWithoutRet();
            return null;
        }
        protected abstract void executeWithoutRet() throws Exception;
    }

    public abstract class LockCallbackWithRet<T> implements LockCallback<T>{
        public final T execute() throws Exception {
            return executeWithRet();
        }
        protected abstract T executeWithRet() throws Exception;
    }

    // 添加新的方法用于处理Hash结构
    public void hset(String key, String field, String value) {
        stringRedisTemplate.opsForHash().put(key, field, value);
    }

    public String hget(String key, String field) {
        Object val = stringRedisTemplate.opsForHash().get(key, field);
        return val != null ? val.toString() : null;
    }

    public Long hincrBy(String key, String field, long increment) {
        return stringRedisTemplate.opsForHash().increment(key, field, increment);
    }

    public Map<Object, Object> hgetAll(String key) {
        return stringRedisTemplate.opsForHash().entries(key);
    }

    public void multiSet(Map<String, String> map) {
        stringRedisTemplate.opsForValue().multiSet(map);
    }

    public Set<String> keys(String pattern) {
        return stringRedisTemplate.keys(pattern);
    }

    public void executeInTransaction(Consumer<StringRedisTemplate> action) {
        stringRedisTemplate.execute(new SessionCallback<Void>() {
            @Override
            public Void execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                action.accept(stringRedisTemplate);
                operations.exec();
                return null;
            }
        });
    }
}
