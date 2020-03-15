package com.distributedLock.aop;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.async.RedisScriptingAsyncCommands;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class DistributedLock {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    private static String ERROR_MSG = "请您使用springBoot2.x版本";
    /**
     * LUA 表达式删除key 能保持原子性
     */
    private static final String UNLOCK_LUA;

    /**
     * 首先获取锁对应的value值，检查是否与requestId相等，如果相等则删除锁（解锁）
     * 并使参数KEYS[1]赋值为lockKey，ARGV[1]赋值为requestId
     */
    static {
        StringBuilder sb = new StringBuilder();
        sb.append("if redis.call(\"get\",KEYS[1]) == ARGV[1] ");
        sb.append("then ");
        sb.append("    return redis.call(\"del\",KEYS[1]) ");
        sb.append("else ");
        sb.append("    return 0 ");
        sb.append("end ");
        UNLOCK_LUA = sb.toString();
    }


    /**
     * 获取锁
     * @param lockKey 加锁的key
     * @param milliSeconds 存活时间单位：毫秒
     * @return boolean 如果锁成功则返回true 否则返回false
     */
    public boolean getLock(String lockKey,String requestId,long milliSeconds){
        boolean lockStatus = false;
        try {
            lockStatus =  stringRedisTemplate.opsForValue().setIfAbsent(lockKey,requestId,milliSeconds,TimeUnit.MILLISECONDS);//相当于setNX
            log.info("lockKey：{} 获取锁{}",lockKey,lockStatus?"成功":"失败");
        } catch (Exception e) {
            log.error("获取锁出现异常：",e);
        }
        return lockStatus;
    }


    /**
     * 释放锁
     * @param lockKey
     * @return boolean
     */
    public boolean unLock(final String lockKey,final String requestId){
        Long status=null;
        try {
            status = stringRedisTemplate.execute(new RedisCallback<Long>() {
                @Override
                public Long doInRedis(RedisConnection connection)
                        throws DataAccessException {
                    Object nativeConnection = connection.getNativeConnection();
                    byte[] keyBytes = lockKey.getBytes(StandardCharsets.UTF_8);
                    byte[] valueBytes = requestId.getBytes(StandardCharsets.UTF_8);
                    Object[] keyParam = new Object[]{keyBytes};
                    if (nativeConnection instanceof RedisScriptingAsyncCommands) {
                        RedisScriptingAsyncCommands<Object,byte[]> command = (RedisScriptingAsyncCommands<Object,byte[]>) nativeConnection;
                        RedisFuture future = command.eval(UNLOCK_LUA, ScriptOutputType.INTEGER, keyParam, valueBytes);
                        Long result = futureGet(future);
                        log.info("key：{},{}",lockKey,(result!=null&&result>0)?"已释放锁":"未释放锁");
                        return result;
                    }else{
                        log.error(ERROR_MSG);
                        return 0L;
                    }
                }
            });
        } catch (Exception e) {
            log.error("释放锁出现异常：",e);
        }
        return (status!=null&&status>0);
    }

    /**
     * 获取解锁结果
     * @param future
     * @return
     */
    private Long futureGet(RedisFuture future){
        try {
            return (Long)future.get();
        } catch (InterruptedException |ExecutionException e) {
            log.error("释放锁出现异常：",e);
        }
        return 0L;
    }


    /**
     * 获取请求id，可做为加锁和解锁方法的requestId参数传递
     * @return String
     */
    public String getRequestId(){
        return UUID.randomUUID().toString();
    }

}