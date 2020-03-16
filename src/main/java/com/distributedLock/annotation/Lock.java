package com.distributedLock.annotation;

import com.distributedLock.enums.LockParameterIndex;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 分布式锁的注解
 * 只能在方法中使用
 * @Classname Lock
 * @Description TODO
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Lock {
    /**
     * 参数坐标 默认为第一个参数下标值为0
     * @return LockParameterIndex
     */
    public LockParameterIndex lockParameterIndex() default LockParameterIndex.INDEX_0;
    /**
     * 参数属性名称，作为锁的键
     * @return String
     */
    public String fieldNameAsLockKey() default "";
	/**
	 * 锁的存活时间默认为300s
	 * @return long
	 */
    public long timeExistence() default 300*1000;
    /**
     * 锁的key值前缀
     * @return String
     */
    public String lockKeyPrefix() default "";
}
