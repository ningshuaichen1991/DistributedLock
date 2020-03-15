package com.distributedLock.aop;
import	java.lang.reflect.Field;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.distributedLock.annotation.Lock;
import com.distributedLock.common.StringUtil;
import com.distributedLock.enums.LockParameterIndex;
import com.distributedLock.exception.FailedAcquireLockException;
import com.distributedLock.exception.LockAnnotationException;
import com.distributedLock.exception.LockException;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * 分布式锁的切入类
 * @Classname DistributedLockAop
 * @Description TODO
 * @Date 2019/7/31 10:24
 * @Created by csn
 */
@Component
@Aspect
public class DistributedLockAop {

    @Resource
    private DistributedLock distributedLock;

    private static Logger logger = LoggerFactory.getLogger(DistributedLockAop.class);


    /**
     * 功能描述: <br>
     * 〈切面逻辑实现〉
     * @Param: [joinPoint]
     * @Return: void
     * @Author: csn
     * @Date: 2019/8/1 19:11
     */
    @Around(value = "pointcut()")
    public Object proccess(ProceedingJoinPoint joinPoint) throws LockException, Exception {
    	logger.info("开始执行切面拦截锁的获取：{}",joinPoint.getSignature().toString());
    	String lockKey = null;
    	String requestId = distributedLock.getRequestId();
    	String lockKeyPrefix = null;
    	String methodName = null;
    	boolean proceedStatus = false;//如果是aop拦截执行完毕则为true
        try {
        	MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        	Method method = methodSignature.getMethod();
        	Signature signature =  joinPoint.getSignature();
        	Lock lock = method.getAnnotation(Lock.class);
			methodName = method.getName();
			lockKeyPrefix = (StringUtils.isEmpty(lock.lockKeyPrefix())?method.getName():lock.lockKeyPrefix());
        	/**
        	 * 1、如果joinPoint.getArgs()不存在，则lockKey=方法的名称+方法名称的hashCode
			 * 2、如果fieldNameAsLockKey为null或者空字符，lockKey默认：方法名+参数类型的hashCode，
        	 * 温馨提示：以上两种情况锁的粒度比较粗，全局只能一个线程能进入这个方法
        	 */
        	String parameterTypesHashCode = (joinPoint.getArgs()==null||joinPoint.getArgs().length ==0)?"":String.valueOf(method.getParameterTypes().hashCode());
        	if(StringUtils.isEmpty(parameterTypesHashCode)){//参数为空
				lockKey = joinPoint.getTarget().getClass().getName()+"."+lockKeyPrefix;
			}else if(StringUtils.isEmpty(lock.fieldNameAsLockKey())) {
        		lockKey = joinPoint.getTarget().getClass().getName()+"."+lockKeyPrefix+parameterTypesHashCode;
        	}else {//如果fieldNameAsLockKey不为空，则要获取第几个参数中的哪一个字段作为lockKey
        		if(joinPoint.getArgs()==null||joinPoint.getArgs().length ==0){
					logger.info("method：{}，参数不存在，请检查fieldNameAsLockKey是否填写正确",methodName);
					throw new LockAnnotationException("method："+methodName+"，fieldNameAsLockKey："+lock.fieldNameAsLockKey()+"有误，在参数类中不存中");
				}
				LockParameterIndex paramObjectIndex = lock.lockParameterIndex();
				if(paramObjectIndex.getIndex()+1>joinPoint.getArgs().length){//如果参数下标值大于方法的参数则抛出异常
					logger.info("method：{}，lockParameterIndex有误，参数类的下标不能大于方法的参数个数",methodName,lock.fieldNameAsLockKey());
					throw new LockAnnotationException("method："+methodName+"，lockParameterIndex有误，参数类的下标不能大于方法的参数个数");
				}
        		Object paramObject = joinPoint.getArgs()[paramObjectIndex.getIndex()];
        		Class<?> param = paramObject.getClass();
        		if(param.isPrimitive()||this.isBasePackage(param.getName())) {//如果是基础类型或者是基础类型的包装类型则直接获取param值
					lockKey = lockKeyPrefix + paramObject.toString();
				}else if(paramObject instanceof Map){//如果参数是map类型的
					lockKey = this.getMapLockKey(lock,paramObject,methodName,lockKeyPrefix);
				}else{//其他类则获取fieldNameAsLockKey的值
					lockKey = this.getObjectLockKey(lock,paramObject,methodName,lockKeyPrefix);
				}
        	}
			boolean getLock = distributedLock.getLock(lockKey,requestId,lock.timeExistence());
			if(!getLock){
				logger.info("method：{}，lockKey：{}获取锁失败",methodName,lockKey);
				throw new FailedAcquireLockException("method："+methodName+"，lockKey："+lockKey+"获取锁失败");
			}
			logger.info("method：{}，锁超时时间为：{}毫秒，lockKey：{}获取锁成功",methodName,lock.timeExistence(),lockKey);
			proceedStatus=true;
            return joinPoint.proceed();//执行方法，业务方法如果出现异常则直接向上层抛
        } catch (Throwable throwable) {
            logger.info("method："+methodName+(proceedStatus?"，业务出现异常":"，分布式锁出现异常"));
			if(throwable instanceof RuntimeException){
				throw  ((RuntimeException)throwable);
			}else if(throwable instanceof Exception){
				throw  ((Exception)throwable);
			}
            throw  new LockException(throwable);
        }finally {
        	if(!StringUtils.isEmpty(lockKey)){
				distributedLock.unLock(lockKey,requestId);
			}
		}
    }


    /**
     * 功能描述: <br>
     * 〈获取map中LockKey〉
     * @Param: [lock, paramObject, methodName, lockKeyPrefix]
     * @Return: java.lang.String
     * @Author: csn
     * @Date: 2019/8/2 18:50
     */
    private String getMapLockKey(Lock lock,Object paramObject,String methodName,String lockKeyPrefix) throws LockException {
		Map mapObject = (Map)paramObject;
		if(!mapObject.containsKey(lock.fieldNameAsLockKey())){
			logger.info("method：{}，fieldNameAsLockKey：{}有误，在参数Map中不存在",methodName,lock.fieldNameAsLockKey());
			throw new LockAnnotationException("method："+methodName+"，fieldNameAsLockKey："+lock.fieldNameAsLockKey()+"有误，在参数类中不存中");
		}
		return lockKeyPrefix+mapObject.get(lock.fieldNameAsLockKey());
	}

	/**
	 * 功能描述: <br>
	 * 〈获取类中的LockKey〉
	 * @Param: [lock, paramObject, methodName, lockKeyPrefix]
	 * @Return: java.lang.String
	 * @Author: csn
	 * @Date: 2019/8/2 18:56
	 */
	public String getObjectLockKey(Lock lock,Object paramObject,String methodName,String lockKeyPrefix) throws LockException, IllegalAccessException {
		Class<?> param = paramObject.getClass();
		Field field =  Arrays.stream(param.getDeclaredFields()).filter(c->lock.fieldNameAsLockKey().equals(c.getName())).findFirst().orElse(null);
		if(field == null){
			logger.info("method：{}，fieldNameAsLockKey：{}有误，在参数类中不存在",methodName,lock.fieldNameAsLockKey());
			throw new LockAnnotationException("method："+methodName+"，fieldNameAsLockKey："+lock.fieldNameAsLockKey()+"有误，在参数类中不存中");
		}
		field.setAccessible(true);//私有变量设置为有权限访问
		if(StringUtil.isNull(field.get(paramObject))){
			logger.info("method：{}，fieldNameAsLockKey：{}参数值为空",methodName,lock.fieldNameAsLockKey());
			throw new LockAnnotationException("method："+methodName+"，fieldNameAsLockKey："+lock.fieldNameAsLockKey()+"参数值为空");
		}
		return lockKeyPrefix+field.get(paramObject);
	}

	/**
	 * 功能描述: <br>
	 * 〈是否为从基础包装类型〉
	 * @Param: [type]
	 * @Return: boolean
	 * @Author: csn
	 * @Date: 2019/8/1 17:42
	 */
    private boolean isBasePackage(String type){
		List<String> basePackages = Arrays.asList("java.lang.String","java.lang.Integer","java.lang.Double","java.lang.Float","java.lang.Long",
				"java.lang.Short","java.lang.Boolean","java.lang.Char","java.math.BigDecimal","java.math.BigInteger");
		return basePackages.contains(type);
	}

    @Pointcut("@annotation(com.distributedLock.annotation.Lock)")
    public void pointcut(){}
}
