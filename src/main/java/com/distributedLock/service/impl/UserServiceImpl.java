package com.distributedLock.service.impl;

import com.distributedLock.annotation.Lock;
import com.distributedLock.domain.User;
import com.distributedLock.enums.LockParameterIndex;
import com.distributedLock.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 用户信息接口实现
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    /**
     * lockParameterIndex：insertUser方法的第一个参数，与fieldNameAsLockKey结合使用的，
     * fieldNameAsLockKey：要setNX的参数类中key是哪一个属性的值，我们这里使用的是User类中的userName属性
     * 也就是说通过fieldNameAsLockKey与lockParameterIndex就能确定是哪一个参数的哪一个属性的值做为setNX的key
     * timeExistence：锁的实效时间，单位是秒，也就是500秒
     * lockKeyPrefix：key值的前缀，也就是该key的数据格式是addUser_*****
     * @param user
     * @param params
     * @return
     */
    @Lock(lockParameterIndex = LockParameterIndex.INDEX_0,fieldNameAsLockKey="userName",
            timeExistence = 500,lockKeyPrefix = "addUser_")
    @Override
    public int insertUser(User user, Map<String, Object> params) {
        log.info("UserName:{}开始执行添加方法……",user.getUserName());
        return 1;
    }
}