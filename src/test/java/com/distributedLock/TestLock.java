package com.distributedLock;

import com.distributedLock.aop.DistributedLock;
import com.distributedLock.domain.User;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import com.distributedLock.service.UserService;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class TestLock {


    @Resource
    DistributedLock distributedLock;

    @Resource
    UserService userService;

    @Test
    public void testLock() throws InterruptedException {

        CountDownLatch c = new CountDownLatch(10);
        for(int i = 0;i<10;i++){
            new Thread(()->{
              userService.insertUser(new User("zhangsan","111111"), Maps.newHashMap());
            }).start();
        }
        c.await();
        Thread.sleep(20000000);
//        String requestId = distributedLock.getRequestId();
//        boolean b = distributedLock.getLock("30",requestId,100000);
//        distributedLock.unLock("30",requestId);
    }

}